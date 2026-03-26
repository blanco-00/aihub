"""MCP Tool model for database persistence."""
from sqlalchemy import Column, Integer, String, Text, DateTime
from sqlalchemy.sql import func
from ..database import Base


class MCPTool(Base):
    """Model for storing MCP tool configurations and metadata."""
    
    __tablename__ = "mcp_tool"

    id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(String(100), nullable=False, unique=True, comment="Tool name")
    description = Column(Text, nullable=False, comment="Tool description")
    input_schema = Column(Text, nullable=False, comment="JSON schema for input parameters")
    tool_type = Column(String(50), nullable=False, default="builtin", comment="Tool type: builtin, remote, custom")
    handler_module = Column(String(255), comment="Python module path for handler function")
    handler_function = Column(String(100), comment="Handler function name")
    endpoint_url = Column(String(500), comment="Remote endpoint URL for remote tools")
    is_enabled = Column(Integer, default=1, comment="1=enabled, 0=disabled")
    execution_count = Column(Integer, default=0, comment="Total execution count")
    last_executed_at = Column(DateTime, comment="Last execution timestamp")
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())
    is_deleted = Column(Integer, default=0, comment="Soft delete flag")

    def __repr__(self):
        return f"<MCPTool(id={self.id}, name='{self.name}', type='{self.tool_type}')>"
