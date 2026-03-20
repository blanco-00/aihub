from sqlalchemy import Column, Integer, String, Text, DateTime
from sqlalchemy.sql import func
from .database import Base


class ModelConfig(Base):
    __tablename__ = "model_config"

    id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(String(255), nullable=False)
    vendor = Column(String(50), nullable=False)
    model_id = Column(String(100), nullable=False)
    api_key = Column(String(500), nullable=False)
    base_url = Column(String(255))
    status = Column(Integer, default=1)
    config = Column(Text)
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())
    is_deleted = Column(Integer, default=0)
