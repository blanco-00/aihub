<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import {
  getDocuments,
  uploadDocument,
  deleteDocument,
  searchDocuments,
  type Document,
  type SearchResult,
} from "@/api/rag";

defineOptions({ name: "AiRag" });

const loading = ref(false);
const documents = ref<Document[]>([]);
const uploadLoading = ref(false);
const uploadDialogVisible = ref(false);
const uploadForm = ref({
  name: "",
  content: "",
  contentType: "text/plain",
});
const searchQuery = ref("");
const searchResults = ref<SearchResult[]>([]);
const searchLoading = ref(false);

const contentTypes = [
  { label: "纯文本", value: "text/plain" },
  { label: "Markdown", value: "text/markdown" },
  { label: "HTML", value: "text/html" },
];

const loadDocuments = async () => {
  loading.value = true;
  try {
    documents.value = await getDocuments();
  } catch (error: any) {
    ElMessage.error("加载文档失败: " + error.message);
  } finally {
    loading.value = false;
  }
};

const handleUpload = async () => {
  if (!uploadForm.value.name.trim()) {
    ElMessage.warning("请输入文档名称");
    return;
  }
  if (!uploadForm.value.content.trim()) {
    ElMessage.warning("请输入文档内容");
    return;
  }

  uploadLoading.value = true;
  try {
    await uploadDocument({
      name: uploadForm.value.name,
      content: uploadForm.value.content,
      fileType: uploadForm.value.contentType,
      knowledgeBaseId: 0,
    });
    ElMessage.success("文档上传成功");
    uploadDialogVisible.value = false;
    uploadForm.value = { name: "", content: "", contentType: "text/plain" };
    await loadDocuments();
  } catch (error: any) {
    ElMessage.error("上传失败: " + error.message);
  } finally {
    uploadLoading.value = false;
  }
};

const handleDelete = async (doc: Document) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文档「${doc.name}」吗？`,
      "提示",
      { type: "warning" }
    );
    await deleteDocument(doc.id);
    ElMessage.success("删除成功");
    await loadDocuments();
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error("删除失败: " + error.message);
    }
  }
};

const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    ElMessage.warning("请输入搜索关键词");
    return;
  }

  searchLoading.value = true;
  try {
    searchResults.value = await searchDocuments({
      query: searchQuery.value,
      limit: 10,
    });
  } catch (error: any) {
    ElMessage.error("搜索失败: " + error.message);
  } finally {
    searchLoading.value = false;
  }
};

const formatSize = (size: number) => {
  if (size < 1024) return size + " B";
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + " KB";
  return (size / (1024 * 1024)).toFixed(1) + " MB";
};

onMounted(() => {
  loadDocuments();
});
</script>

<template>
  <div class="rag-container">
    <el-row :gutter="20">
      <el-col :span="10">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>文档管理</span>
              <el-button
                type="primary"
                :icon="useRenderIcon('ep:plus')"
                @click="uploadDialogVisible = true"
              >
                上传文档
              </el-button>
            </div>
          </template>

          <el-table
            v-loading="loading"
            :data="documents"
            style="width: 100%"
            max-height="calc(100vh - 280px)"
          >
            <el-table-column prop="name" label="文档名称" min-width="150" />
            <el-table-column prop="fileType" label="类型" width="100" />
            <el-table-column prop="chunkCount" label="Chunks" width="80" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button
                  text
                  type="danger"
                  size="small"
                  :icon="useRenderIcon('ep:delete')"
                  @click="handleDelete(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card class="box-card">
          <template #header>
            <span>文档搜索</span>
          </template>

          <div class="search-box">
            <el-input
              v-model="searchQuery"
              placeholder="输入关键词搜索文档..."
              size="large"
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button
                  :icon="useRenderIcon('ep:search')"
                  :loading="searchLoading"
                  @click="handleSearch"
                >
                  搜索
                </el-button>
              </template>
            </el-input>
          </div>

          <div v-if="searchResults.length > 0" class="search-results">
            <div
              v-for="(result, index) in searchResults"
              :key="index"
              class="result-item"
            >
              <div class="result-header">
                <span class="result-title">{{ result.documentName }}</span>
                <el-tag size="small" type="info">
                  相似度: {{ (result.score * 100).toFixed(1) }}%
                </el-tag>
              </div>
              <div class="result-content">{{ result.chunkContent }}</div>
            </div>
          </div>

          <el-empty
            v-else-if="!searchLoading && searchQuery"
            description="未找到相关文档"
          />

          <el-empty
            v-else-if="!searchLoading"
            description="输入关键词开始搜索"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="uploadDialogVisible"
      title="上传文档"
      width="600px"
    >
      <el-form label-width="80px">
        <el-form-item label="文档名称">
          <el-input
            v-model="uploadForm.name"
            placeholder="请输入文档名称"
          />
        </el-form-item>
        <el-form-item label="内容类型">
          <el-select v-model="uploadForm.contentType" style="width: 100%">
            <el-option
              v-for="item in contentTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="文档内容">
          <el-input
            v-model="uploadForm.content"
            type="textarea"
            :rows="10"
            placeholder="请输入文档内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="uploadLoading"
          @click="handleUpload"
        >
          上传
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.rag-container {
  padding: 20px;
}

.box-card {
  height: calc(100vh - 140px);
  overflow: hidden;
  display: flex;
  flex-direction: column;

  :deep(.el-card__body) {
    flex: 1;
    overflow-y: auto;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-box {
  margin-bottom: 20px;
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-item {
  padding: 12px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);

  .result-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  .result-title {
    font-weight: 500;
    color: var(--el-color-primary);
  }

  .result-content {
    font-size: 14px;
    color: var(--el-text-color-regular);
    line-height: 1.6;
  }
}
</style>
