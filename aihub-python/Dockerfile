FROM python:3.10-slim

WORKDIR /app

COPY pyproject.toml .
RUN pip install .

COPY src/ ./src/

EXPOSE 8000

CMD ["uvicorn", "src.aihub.main:app", "--host", "0.0.0.0", "--port", "8000"]
