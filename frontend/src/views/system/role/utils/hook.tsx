import dayjs from "dayjs";
import editForm from "../form.vue";
import { handleTree } from "@/utils/tree";
import { message } from "@/utils/message";
import { ElMessageBox } from "element-plus";
import { usePublicHooks } from "../../hooks";
import { transformI18n } from "@/plugins/i18n";
import { addDialog } from "@/components/ReDialog";
import type { FormItemProps } from "../utils/types";
import type { PaginationProps } from "@pureadmin/table";
import { getKeyList, deviceDetection } from "@pureadmin/utils";
import { getAllRoles, createRole, updateRole, deleteRole, getRoleMenus, saveRoleMenus, type CreateRoleRequest, type UpdateRoleRequest } from "@/api/role";
import { getMenuTree } from "@/api/menu";
import { type Ref, reactive, ref, onMounted, h, toRaw, watch } from "vue";

export function useRole(treeRef: Ref) {
  const form = reactive({
    name: "",
    code: "",
    status: ""
  });
  const curRow = ref();
  const formRef = ref();
  const dataList = ref([]);
  const treeIds = ref([]);
  const treeData = ref([]);
  const isShow = ref(false);
  const loading = ref(true);
  const isLinkage = ref(false);
  const treeSearchValue = ref();
  const switchLoadMap = ref({});
  const isExpandAll = ref(false);
  const isSelectAll = ref(false);
  const { switchStyle } = usePublicHooks();
  const treeProps = {
    value: "id",
    label: "title",
    children: "children"
  };
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });
  const columns: TableColumnList = [
    {
      label: "角色编号",
      prop: "id"
    },
    {
      label: "角色名称",
      prop: "name"
    },
    {
      label: "角色标识",
      prop: "code"
    },
    {
      label: "状态",
      cellRenderer: scope => (
        <el-switch
          size={scope.props.size === "small" ? "small" : "default"}
          loading={switchLoadMap.value[scope.index]?.loading}
          v-model={scope.row.status}
          active-value={1}
          inactive-value={0}
          active-text="已启用"
          inactive-text="已停用"
          inline-prompt
          style={switchStyle.value}
          onChange={() => onChange(scope as any)}
        />
      ),
      minWidth: 90
    },
    {
      label: "备注",
      prop: "description",
      minWidth: 160
    },
    {
      label: "创建时间",
      prop: "createdAt",
      minWidth: 160,
      formatter: ({ createdAt }) =>
        createdAt ? dayjs(createdAt).format("YYYY-MM-DD HH:mm:ss") : "-"
    },
    {
      label: "操作",
      fixed: "right",
      width: 210,
      slot: "operation"
    }
  ];
  // const buttonClass = computed(() => {
  //   return [
  //     "h-[20px]!",
  //     "reset-margin",
  //     "text-gray-500!",
  //     "dark:text-white!",
  //     "dark:hover:text-primary!"
  //   ];
  // });

  async function onChange({ row, index }) {
    ElMessageBox.confirm(
      `确认要<strong>${
        row.status === 0 ? "停用" : "启用"
      }</strong><strong style='color:var(--el-color-primary)'>${
        row.name
      }</strong>吗?`,
      "系统提示",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
        dangerouslyUseHTMLString: true,
        draggable: true
      }
    )
      .then(async () => {
        switchLoadMap.value[index] = Object.assign(
          {},
          switchLoadMap.value[index],
          {
            loading: true
          }
        );
        try {
          const newStatus = row.status === 0 ? 1 : 0;
          const request: UpdateRoleRequest = { status: newStatus };
          const response = await updateRole(row.id, request);
          if (response.code === 200) {
            row.status = newStatus;
            message(`已${row.status === 0 ? "停用" : "启用"}${row.name}`, {
              type: "success"
            });
          } else {
            message(response.message || "更新角色状态失败", { type: "error" });
            row.status === 0 ? (row.status = 1) : (row.status = 0);
          }
        } catch (error: any) {
          message(error.message || "更新角色状态失败", { type: "error" });
          row.status === 0 ? (row.status = 1) : (row.status = 0);
        } finally {
          switchLoadMap.value[index] = Object.assign(
            {},
            switchLoadMap.value[index],
            {
              loading: false
            }
          );
        }
      })
      .catch(() => {
        row.status === 0 ? (row.status = 1) : (row.status = 0);
      });
  }

  async function handleDelete(row) {
    try {
      const response = await deleteRole(row.id);
      if (response.code === 200) {
        message(`您删除了角色名称为${row.name}的这条数据`, { type: "success" });
        onSearch();
      } else {
        message(response.message || "删除角色失败", { type: "error" });
      }
    } catch (error: any) {
      message(error.message || "删除角色失败", { type: "error" });
    }
  }

  function handleSizeChange(val: number) {
    // 调试用，已注释
    // console.log(`${val} items per page`);
  }

  function handleCurrentChange(val: number) {
    // 调试用，已注释
    // console.log(`current page: ${val}`);
  }

  function handleSelectionChange(val) {
    // 调试用，已注释
    // console.log("handleSelectionChange", val);
  }

  async function onSearch() {
    loading.value = true;
    try {
      const searchStartTime = performance.now();
      
      const response = await getAllRoles();
      const searchTime = performance.now() - searchStartTime;
      
      // 只记录超过1秒的请求
      if (searchTime > 1000) {
        console.warn(`[性能警告] 角色列表查询耗时: ${searchTime.toFixed(2)}ms`);
      }
      // 后端返回 code: 200 表示成功
      if (response.code === 200 && response.data) {
        let roles = response.data;
        
        // 前端筛选
        if (form.name) {
          roles = roles.filter((item: any) => item.name.includes(form.name));
        }
        if (form.code) {
          roles = roles.filter((item: any) => item.code.includes(form.code));
        }
        if (form.status !== "") {
          roles = roles.filter((item: any) => item.status === Number(form.status));
        }
        
        dataList.value = roles;
        pagination.total = roles.length;
        pagination.pageSize = 10;
        pagination.currentPage = 1;
      } else {
        message(response.message || "获取角色列表失败", { type: "error" });
        dataList.value = [];
      }
    } catch (error: any) {
      message(error.message || "获取角色列表失败", { type: "error" });
      dataList.value = [];
    } finally {
      loading.value = false;
    }
  }

  const resetForm = formEl => {
    if (!formEl) return;
    formEl.resetFields();
    onSearch();
  };

  function openDialog(title = "新增", row?: FormItemProps) {
    addDialog({
      title: `${title}角色`,
      props: {
        formInline: {
          id: row?.id,
          name: row?.name ?? "",
          code: row?.code ?? "",
          remark: row?.remark ?? row?.description ?? ""
        }
      },
      width: "40%",
      draggable: true,
      fullscreen: deviceDetection(),
      fullscreenIcon: true,
      closeOnClickModal: false,
      contentRenderer: () => h(editForm, { ref: formRef, formInline: null }),
      beforeSure: async (done, { options }) => {
        const FormRef = formRef.value.getRef();
        const curData = options.props.formInline as FormItemProps;
        FormRef.validate(async (valid: boolean) => {
          if (valid) {
            try {
              if (title === "新增") {
                const request: CreateRoleRequest = {
                  code: curData.code,
                  name: curData.name,
                  description: curData.remark || undefined,
                  status: 1
                };
                const response = await createRole(request);
                if (response.code === 200) {
                  message(`您${title}了角色名称为${curData.name}的这条数据`, {
                    type: "success"
                  });
                  done();
                  onSearch();
                } else {
                  message(response.message || "创建角色失败", { type: "error" });
                }
              } else {
                const request: UpdateRoleRequest = {
                  name: curData.name || undefined,
                  description: curData.remark || undefined
                };
                const response = await updateRole(curData.id, request);
                if (response.code === 200) {
                  message(`您${title}了角色名称为${curData.name}的这条数据`, {
                    type: "success"
                  });
                  done();
                  onSearch();
                } else {
                  message(response.message || "更新角色失败", { type: "error" });
                }
              }
            } catch (error: any) {
              message(error.message || `${title}角色失败`, { type: "error" });
            }
          }
        });
      }
    });
  }

  /** 菜单权限 */
  async function handleMenu(row?: any) {
    const { id } = row;
    if (id) {
      curRow.value = row;
      isShow.value = true;
      
      // 从后端获取已选中的菜单ID
      try {
        const response = await getRoleMenus(id);
        if (response.code === 200 && response.data) {
          treeRef.value.setCheckedKeys(response.data);
        } else {
          treeRef.value.setCheckedKeys([]);
        }
      } catch (error: any) {
        message(error.message || "获取角色菜单权限失败", { type: "error" });
        treeRef.value.setCheckedKeys([]);
      }
    } else {
      curRow.value = null;
      isShow.value = false;
    }
  }

  /** 高亮当前权限选中行 */
  function rowStyle({ row: { id } }) {
    return {
      cursor: "pointer",
      background: id === curRow.value?.id ? "var(--el-fill-color-light)" : ""
    };
  }

  /** 菜单权限-保存 */
  async function handleSave() {
    const { id, name } = curRow.value;
    const checkedKeys = treeRef.value.getCheckedKeys();
    try {
      const response = await saveRoleMenus(id, checkedKeys);
      if (response.code === 200) {
        message(`角色名称为${name}的菜单权限修改成功`, { type: "success" });
      } else {
        message(response.message || "保存菜单权限失败", { type: "error" });
      }
    } catch (error: any) {
      message(error.message || "保存菜单权限失败", { type: "error" });
    }
  }

  /** 数据权限 可自行开发 */
  // function handleDatabase() {}

  const onQueryChanged = (query: string) => {
    treeRef.value!.filter(query);
  };

  const filterMethod = (query: string, node) => {
    return transformI18n(node.title)!.includes(query);
  };

  onMounted(async () => {
    // 优化：先等待第一个请求完成，避免同时建立多个连接导致的延迟
    // 这样可以复用第一个请求建立的连接
    await onSearch();
    
    // 获取菜单树用于权限配置（在第一个请求完成后发起，可以复用连接）
    try {
      const response = await getMenuTree();
      if (response.code === 200 && response.data) {
        treeIds.value = getKeyList(response.data, "id");
        treeData.value = handleTree(response.data);
      }
    } catch (error: any) {
      message(error.message || "获取菜单列表失败", { type: "error" });
    }
  });

  watch(isExpandAll, val => {
    val
      ? treeRef.value.setExpandedKeys(treeIds.value)
      : treeRef.value.setExpandedKeys([]);
  });

  watch(isSelectAll, val => {
    val
      ? treeRef.value.setCheckedKeys(treeIds.value)
      : treeRef.value.setCheckedKeys([]);
  });

  return {
    form,
    isShow,
    curRow,
    loading,
    columns,
    rowStyle,
    dataList,
    treeData,
    treeProps,
    isLinkage,
    pagination,
    isExpandAll,
    isSelectAll,
    treeSearchValue,
    // buttonClass,
    onSearch,
    resetForm,
    openDialog,
    handleMenu,
    handleSave,
    handleDelete,
    filterMethod,
    transformI18n,
    onQueryChanged,
    // handleDatabase,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange
  };
}
