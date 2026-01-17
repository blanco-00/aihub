import { delay } from "@pureadmin/utils";
import { ref, watch, reactive, type Ref } from "vue";
import type { PaginationProps } from "@pureadmin/table";
import ThumbUp from "~icons/ri/thumb-up-line";
import Hearts from "~icons/ri/hearts-line";
import Empty from "./empty.svg?component";

export function useColumns(tableData?: Ref<any[]>) {
  const dataList = ref([]);
  const loading = ref(true);
  
  // 监听外部传入的tableData
  if (tableData) {
    watch(
      tableData,
      (newData) => {
        if (newData && newData.length > 0) {
          dataList.value = newData;
          pagination.total = newData.length;
          loading.value = false;
        }
      },
      { immediate: true }
    );
  }
  const columns: TableColumnList = [
    {
      sortable: true,
      label: "序号",
      prop: "id"
    },
    {
      sortable: true,
      label: "需求人数",
      prop: "requiredNumber",
      filterMultiple: false,
      filterClassName: "pure-table-filter",
      filters: [
        { text: "≥16000", value: "more" },
        { text: "<16000", value: "less" }
      ],
      filterMethod: (value, { requiredNumber }) => {
        return value === "more"
          ? requiredNumber >= 16000
          : requiredNumber < 16000;
      }
    },
    {
      sortable: true,
      label: "提问数量",
      prop: "questionNumber"
    },
    {
      sortable: true,
      label: "解决数量",
      prop: "resolveNumber"
    },
    {
      sortable: true,
      label: "用户满意度",
      minWidth: 100,
      prop: "satisfaction",
      cellRenderer: ({ row }) => (
        <div class="flex justify-center w-full">
          <span class="flex items-center w-[60px]">
            <span class="ml-auto mr-2">{row.satisfaction}%</span>
            <iconifyIconOffline
              icon={row.satisfaction > 98 ? Hearts : ThumbUp}
              color="#e85f33"
            />
          </span>
        </div>
      )
    },
    {
      sortable: true,
      label: "统计日期",
      prop: "date"
    },
    {
      label: "操作",
      fixed: "right",
      slot: "operation"
    }
  ];

  /** 分页配置 */
  const pagination = reactive<PaginationProps>({
    pageSize: 10,
    currentPage: 1,
    layout: "prev, pager, next",
    total: 0,
    align: "center"
  });

  function onCurrentChange(page: number) {
    console.log("onCurrentChange", page);
    loading.value = true;
    delay(300).then(() => {
      loading.value = false;
    });
  }

  // 如果没有外部数据，保持loading状态
  if (!tableData || !tableData.value || tableData.value.length === 0) {
    loading.value = true;
  }

  return {
    Empty,
    loading,
    columns,
    dataList,
    pagination,
    onCurrentChange
  };
}
