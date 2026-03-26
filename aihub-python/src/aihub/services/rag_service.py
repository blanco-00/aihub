import json
import logging
from typing import List, Dict, Any, Optional, Tuple
from dataclasses import dataclass
import pymysql
import numpy as np

logger = logging.getLogger(__name__)


@dataclass
class Document:
    id: int
    name: str
    content: str
    content_type: str
    chunks: List[str]
    metadata: Dict[str, Any]
    user_id: Optional[int]


@dataclass
class TextChunk:
    text: str
    metadata: Dict[str, Any]
    embedding: Optional[List[float]] = None


class RAGService:
    CHUNK_SIZE = 500
    CHUNK_OVERLAP = 50
    
    def __init__(self):
        self._documents: Dict[int, Document] = {}
        self._embeddings_cache: Dict[int, List[List[float]]] = {}
    
    def _get_connection(self):
        from ..config import get_db_connection
        return get_db_connection()
    
    def _simple_embed(self, texts: List[str]) -> List[List[float]]:
        import hashlib
        result = []
        for text in texts:
            h = hashlib.md5(text.encode()).digest()
            vec = np.array(list(h)[:256] + [0] * (256 - 256)) / 255.0
            vec = vec[:128]
            result.append(vec.tolist())
        return result
    
    def _chunk_text(self, text: str, chunk_size: int = None, overlap: int = None) -> List[str]:
        if chunk_size is None:
            chunk_size = self.CHUNK_SIZE
        if overlap is None:
            overlap = self.CHUNK_OVERLAP
        
        if len(text) <= chunk_size:
            return [text] if text.strip() else []
        
        chunks = []
        start = 0
        while start < len(text):
            end = start + chunk_size
            chunk = text[start:end]
            if chunk.strip():
                chunks.append(chunk)
            start = end - overlap
        
        return chunks
    
    def _cosine_similarity(self, a: List[float], b: List[float]) -> float:
        a = np.array(a)
        b = np.array(b)
        norm_a = np.linalg.norm(a)
        norm_b = np.linalg.norm(b)
        if norm_a == 0 or norm_b == 0:
            return 0.0
        return float(np.dot(a, b) / (norm_a * norm_b))
    
    def parse_document(
        self,
        name: str,
        content: str,
        content_type: str,
        user_id: Optional[int] = None,
        metadata: Optional[Dict[str, Any]] = None
    ) -> Document:
        chunks = self._chunk_text(content)
        
        conn = self._get_connection()
        with conn.cursor() as cur:
            cur.execute(
                """INSERT INTO document (name, content_type, content, chunks, metadata, user_id)
                   VALUES (%s, %s, %s, %s, %s, %s)""",
                (
                    name,
                    content_type,
                    content,
                    json.dumps(chunks, ensure_ascii=False),
                    json.dumps(metadata or {}, ensure_ascii=False),
                    user_id
                )
            )
            doc_id = cur.lastrowid
        conn.close()
        
        doc = Document(
            id=doc_id,
            name=name,
            content=content,
            content_type=content_type,
            chunks=chunks,
            metadata=metadata or {},
            user_id=user_id
        )
        self._documents[doc_id] = doc
        return doc
    
    def get_document(self, doc_id: int) -> Optional[Document]:
        if doc_id in self._documents:
            return self._documents[doc_id]
        
        try:
            conn = self._get_connection()
            with conn.cursor() as cur:
                cur.execute(
                    """SELECT id, name, content, content_type, chunks, metadata, user_id
                       FROM document WHERE id = %s AND is_deleted = 0""",
                    (doc_id,)
                )
                row = cur.fetchone()
            conn.close()
            
            if row:
                chunks = json.loads(row[4]) if row[4] else []
                metadata = json.loads(row[5]) if row[5] else {}
                doc = Document(
                    id=row[0], name=row[1], content=row[2],
                    content_type=row[3], chunks=chunks,
                    metadata=metadata, user_id=row[6]
                )
                self._documents[doc_id] = doc
                return doc
            return None
        except Exception as e:
            logger.error(f"Failed to get document: {e}")
            return None
    
    def search(
        self,
        query: str,
        top_k: int = 5,
        doc_ids: Optional[List[int]] = None
    ) -> List[Tuple[Document, float, str]]:
        query_embedding = self._simple_embed([query])[0]
        
        results = []
        
        if doc_ids:
            docs = [self.get_document(doc_id) for doc_id in doc_ids]
        else:
            docs = list(self._documents.values())
        
        if not docs:
            try:
                conn = self._get_connection()
                with conn.cursor() as cur:
                    cur.execute(
                        """SELECT id, name, content, content_type, chunks, metadata, user_id
                           FROM document WHERE is_deleted = 0 LIMIT 20"""
                    )
                    rows = cur.fetchall()
                conn.close()
                
                for row in rows:
                    chunks = json.loads(row[4]) if row[4] else []
                    metadata = json.loads(row[5]) if row[5] else {}
                    doc = Document(
                        id=row[0], name=row[1], content=row[2],
                        content_type=row[3], chunks=chunks,
                        metadata=metadata, user_id=row[6]
                    )
                    docs.append(doc)
                    self._documents[row[0]] = doc
            except Exception as e:
                logger.error(f"Failed to load documents: {e}")
        
        for doc in docs:
            if not doc or not doc.chunks:
                continue
            
            doc_embeddings = self._simple_embed(doc.chunks)
            
            for chunk_text, chunk_emb in zip(doc.chunks, doc_embeddings):
                sim = self._cosine_similarity(query_embedding, chunk_emb)
                results.append((doc, sim, chunk_text))
        
        results.sort(key=lambda x: x[1], reverse=True)
        return results[:top_k]
    
    def list_documents(self, user_id: Optional[int] = None) -> List[Document]:
        try:
            conn = self._get_connection()
            with conn.cursor() as cur:
                if user_id:
                    cur.execute(
                        """SELECT id, name, content, content_type, chunks, metadata, user_id
                           FROM document WHERE is_deleted = 0 AND user_id = %s
                           ORDER BY created_at DESC LIMIT 50""",
                        (user_id,)
                    )
                else:
                    cur.execute(
                        """SELECT id, name, content, content_type, chunks, metadata, user_id
                           FROM document WHERE is_deleted = 0
                           ORDER BY created_at DESC LIMIT 50"""
                    )
                rows = cur.fetchall()
            conn.close()
            
            docs = []
            for row in rows:
                chunks = json.loads(row[4]) if row[4] else []
                metadata = json.loads(row[5]) if row[5] else {}
                doc = Document(
                    id=row[0], name=row[1], content=row[2],
                    content_type=row[3], chunks=chunks,
                    metadata=metadata, user_id=row[6]
                )
                docs.append(doc)
            return docs
        except Exception as e:
            logger.error(f"Failed to list documents: {e}")
            return []
    
    def delete_document(self, doc_id: int) -> bool:
        try:
            conn = self._get_connection()
            with conn.cursor() as cur:
                cur.execute(
                    "UPDATE document SET is_deleted = 1 WHERE id = %s",
                    (doc_id,)
                )
            conn.close()
            self._documents.pop(doc_id, None)
            return True
        except Exception as e:
            logger.error(f"Failed to delete document: {e}")
            return False


rag_service = RAGService()
