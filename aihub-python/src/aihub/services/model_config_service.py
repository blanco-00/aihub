from typing import List, Optional
from sqlalchemy.orm import Session
from .models.model_config import ModelConfig


class ModelConfigService:
    def __init__(self, db: Session):
        self.db = db

    def get_all(self) -> List[ModelConfig]:
        return self.db.query(ModelConfig).filter(
            ModelConfig.status == 1,
            ModelConfig.is_deleted == 0
        ).all()

    def get_by_id(self, model_id: int) -> Optional[ModelConfig]:
        return self.db.query(ModelConfig).filter(
            ModelConfig.id == model_id,
            ModelConfig.is_deleted == 0
        ).first()

    def get_by_vendor(self, vendor: str) -> List[ModelConfig]:
        return self.db.query(ModelConfig).filter(
            ModelConfig.vendor == vendor,
            ModelConfig.status == 1,
            ModelConfig.is_deleted == 0
        ).all()
