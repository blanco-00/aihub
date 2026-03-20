<script setup lang="ts">
import { ref, watch, onMounted, nextTick, computed } from "vue";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import "highlight.js/styles/github-dark.css";

defineOptions({
  name: "StreamMessage",
});

interface Props {
  content: string;
  isStreaming?: boolean;
  role?: "user" | "assistant" | "system";
}

const props = withDefaults(defineProps<Props>(), {
  isStreaming: false,
  role: "assistant",
});

const messageRef = ref<HTMLElement>();
const displayedContent = ref("");
const cursorVisible = ref(true);

// 简单的 Markdown 解析（如果 marked 库未安装）
const parseMarkdown = (text: string): string => {
  // 代码块
  text = text.replace(
    /```(\w*)\n?([\s\S]*?)```/g,
    (_, lang, code) =>
      `<pre class="code-block"><code class="language-${lang}">${escapeHtml(code.trim())}</code></pre>`,
  );

  // 行内代码
  text = text.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>');

  // 标题
  text = text.replace(/^### (.+)$/gm, "<h3>$1</h3>");
  text = text.replace(/^## (.+)$/gm, "<h2>$1</h2>");
  text = text.replace(/^# (.+)$/gm, "<h1>$1</h1>");

  // 粗体和斜体
  text = text.replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>");
  text = text.replace(/\*(.+?)\*/g, "<em>$1</em>");

  // 列表
  text = text.replace(/^\* (.+)$/gm, "<li>$1</li>");
  text = text.replace(/^- (.+)$/gm, "<li>$1</li>");

  // 链接
  text = text.replace(
    /\[([^\]]+)\]\(([^)]+)\)/g,
    '<a href="$2" target="_blank" rel="noopener">$1</a>',
  );

  // 换行
  text = text.replace(/\n/g, "<br>");

  return text;
};

const escapeHtml = (text: string): string => {
  const map: Record<string, string> = {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#039;",
  };
  return text.replace(/[&<>"']/g, (m) => map[m]);
};

// 渲染后的 HTML 内容
const renderedContent = computed(() => {
  return parseMarkdown(displayedContent.value);
});

// 打字机效果
watch(
  () => props.content,
  async (newContent) => {
    if (props.isStreaming) {
      // 流式显示：逐字符显示
      const diff = newContent.slice(displayedContent.value.length);
      for (const char of diff) {
        displayedContent.value += char;
        await nextTick();
        scrollToBottom();
        await new Promise((resolve) => setTimeout(resolve, 20)); // 20ms 延迟
      }
    } else {
      // 非流式：直接显示
      displayedContent.value = newContent;
      await nextTick();
      scrollToBottom();
    }
  },
  { immediate: true },
);

// 光标闪烁效果
let cursorInterval: number | null = null;

onMounted(() => {
  if (props.isStreaming) {
    cursorInterval = window.setInterval(() => {
      cursorVisible.value = !cursorVisible.value;
    }, 500);
  }
});

// 清理定时器
watch(
  () => props.isStreaming,
  (isStreaming) => {
    if (!isStreaming && cursorInterval) {
      clearInterval(cursorInterval);
      cursorInterval = null;
    }
  },
);

const scrollToBottom = () => {
  if (messageRef.value) {
    const container = messageRef.value.closest(".chat-messages");
    if (container) {
      container.scrollTop = container.scrollHeight;
    }
  }
};

// 复制功能
const copyContent = async () => {
  try {
    await navigator.clipboard.writeText(props.content);
    // 可以添加成功提示
  } catch (err) {
    console.error("Failed to copy:", err);
  }
};

defineExpose({
  scrollToBottom,
});
</script>

<template>
  <div ref="messageRef" class="stream-message" :class="role">
    <div class="message-content" v-html="renderedContent" />
    <span v-if="isStreaming && cursorVisible" class="typing-cursor">|</span>
    <div v-if="!isStreaming && content" class="message-actions">
      <el-button
        text
        size="small"
        :icon="useRenderIcon('ep:document-copy')"
        @click="copyContent"
      >
        复制
      </el-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.stream-message {
  position: relative;
  width: 100%;
}

.message-content {
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;

  :deep(h1) {
    font-size: 1.5em;
    font-weight: bold;
    margin: 0.5em 0;
  }

  :deep(h2) {
    font-size: 1.3em;
    font-weight: bold;
    margin: 0.5em 0;
  }

  :deep(h3) {
    font-size: 1.1em;
    font-weight: bold;
    margin: 0.5em 0;
  }

  :deep(.code-block) {
    background: #1e1e1e;
    border-radius: 4px;
    padding: 12px;
    overflow-x: auto;
    margin: 8px 0;

    code {
      font-family: "Courier New", monospace;
      font-size: 0.9em;
      line-height: 1.5;
    }
  }

  :deep(.inline-code) {
    background: var(--el-fill-color-light);
    padding: 2px 6px;
    border-radius: 3px;
    font-family: "Courier New", monospace;
    font-size: 0.9em;
  }

  :deep(li) {
    margin-left: 1.5em;
    list-style-type: disc;
  }

  :deep(a) {
    color: var(--el-color-primary);
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  :deep(strong) {
    font-weight: bold;
  }

  :deep(em) {
    font-style: italic;
  }
}

.typing-cursor {
  display: inline-block;
  animation: blink 1s infinite;
  color: var(--el-color-primary);
  font-weight: bold;
}

@keyframes blink {
  0%,
  50% {
    opacity: 1;
  }
  51%,
  100% {
    opacity: 0;
  }
}

.message-actions {
  display: none;
  position: absolute;
  top: 0;
  right: 0;
  padding: 4px;
  background: var(--el-bg-color);
  border-radius: 4px;
  box-shadow: var(--el-box-shadow-light);
}

.stream-message:hover .message-actions {
  display: block;
}
</style>
