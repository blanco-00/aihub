<script setup lang="ts">
import { PropType } from "vue";
import { ListItem } from "../data";
import NoticeItem from "./NoticeItem.vue";
import { transformI18n } from "@/plugins/i18n";

const props = defineProps({
  list: {
    type: Array as PropType<Array<ListItem>>,
    default: () => [],
  },
  emptyText: {
    type: String,
    default: "",
  },
});

const emit = defineEmits<{
  (e: "read", noticeId: number): void;
}>();

function handleRead(noticeId: number) {
  emit("read", noticeId);
}
</script>

<template>
  <div v-if="list.length">
    <NoticeItem
      v-for="(item, index) in list"
      :key="index"
      :noticeItem="item"
      @read="handleRead"
    />
  </div>
  <el-empty v-else :description="transformI18n(emptyText)" />
</template>
