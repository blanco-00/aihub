import "./reset.css";
import dayjs from "dayjs";
import roleForm from "../form/role.vue";
import editForm from "../form/index.vue";
import { zxcvbn } from "@zxcvbn-ts/core";
import { message } from "@/utils/message";
import userAvatar from "@/assets/user.jpg";
import { usePublicHooks } from "../../hooks";
import { addDialog } from "@/components/ReDialog";
import type { PaginationProps } from "@pureadmin/table";
import ReCropperPreview from "@/components/ReCropperPreview";
import type { FormItemProps, RoleFormItemProps } from "../utils/types";
import {
  getKeyList,
  isAllEmpty,
  hideTextAtIndex,
  deviceDetection
} from "@pureadmin/utils";
import {
  getRoleIds,
  assignUserRoles,
  getUserList,
  getAllRoleList,
  getAllRolesFromDB,
  createUser,
  updateUser,
  deleteUser,
  toggleUserStatus
} from "@/api/system";
import {
  ElForm,
  ElInput,
  ElFormItem,
  ElProgress,
  ElMessageBox
} from "element-plus";
import {
  type Ref,
  h,
  ref,
  toRaw,
  watch,
  computed,
  reactive,
  onMounted
} from "vue";

export function useUser(tableRef: Ref) {
  const form = reactive({
    username: "",
    phone: "",
    status: "",
    departmentId: undefined as number | undefined
  });
  const formRef = ref();
  const ruleFormRef = ref();
  const roleFormRef = ref();
  const dataList = ref([]);
  const loading = ref(true);
  // 上传头像信息
  const avatarInfo = ref();
  const switchLoadMap = ref({});
  const { switchStyle } = usePublicHooks();
  const selectedNum = ref(0);
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });
  const columns: TableColumnList = [
    {
      label: "勾选列", // 如果需要表格多选，此处label必须设置
      type: "selection",
      fixed: "left",
      reserveSelection: true // 数据刷新后保留选项
    },
    {
      label: "用户编号",
      prop: "id",
      width: 90
    },
    {
      label: "用户头像",
      prop: "avatar",
      cellRenderer: ({ row }) => (
        <el-image
          fit="cover"
          preview-teleported={true}
          src={row.avatar || userAvatar}
          preview-src-list={Array.of(row.avatar || userAvatar)}
          class="w-[24px] h-[24px] rounded-full align-middle"
        />
      ),
      width: 90
    },
    {
      label: "用户名称",
      prop: "username",
      minWidth: 130
    },
    {
      label: "用户昵称",
      prop: "nickname",
      minWidth: 130
    },
    {
      label: "性别",
      prop: "sex",
      minWidth: 90,
      cellRenderer: ({ row, props }) => (
        <el-tag
          size={props.size}
          type={row.sex === 1 ? "danger" : null}
          effect="plain"
        >
          {row.sex === 1 ? "女" : "男"}
        </el-tag>
      )
    },
    {
      label: "手机号码",
      prop: "phone",
      minWidth: 90,
      formatter: ({ phone }) => hideTextAtIndex(phone, { start: 3, end: 6 })
    },
    {
      label: "部门",
      prop: "departmentName",
      minWidth: 120
    },
    {
      label: "角色",
      prop: "roleNames",
      minWidth: 150,
      cellRenderer: ({ row }) => {
        const roleNames = row.roleNames || [];
        if (roleNames.length === 0) {
          return <span class="text-gray-400">未分配</span>;
        }
        return (
          <div class="flex flex-wrap gap-1">
            {roleNames.map((name: string, index: number) => (
              <el-tag
                key={index}
                size="small"
                type={name === "超级管理员" ? "danger" : name === "管理员" ? "warning" : "info"}
                effect="plain"
              >
                {name}
              </el-tag>
            ))}
          </div>
        );
      }
    },
    {
      label: "状态",
      prop: "status",
      minWidth: 90,
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
      )
    },
    {
      label: "创建时间",
      minWidth: 90,
      prop: "createTime",
      formatter: ({ createTime }) =>
        dayjs(createTime).format("YYYY-MM-DD HH:mm:ss")
    },
    {
      label: "备注",
      prop: "remark",
      minWidth: 200,
      showOverflowTooltip: true
    },
    {
      label: "操作",
      fixed: "right",
      width: 180,
      slot: "operation"
    }
  ];
  const buttonClass = computed(() => {
    return [
      "h-[20px]!",
      "reset-margin",
      "text-gray-500!",
      "dark:text-white!",
      "dark:hover:text-primary!"
    ];
  });
  // 重置的新密码
  const pwdForm = reactive({
    newPwd: ""
  });
  const pwdProgress = [
    { color: "#e74242", text: "非常弱" },
    { color: "#EFBD47", text: "弱" },
    { color: "#ffa500", text: "一般" },
    { color: "#1bbf1b", text: "强" },
    { color: "#008000", text: "非常强" }
  ];
  // 当前密码强度（0-4）
  const curScore = ref();
  const roleOptions = ref([]);

  async function onChange({ row, index }) {
    // 先保存原始状态，以便取消时恢复
    const originalStatus = row.status;
    // 计算新状态
    const newStatus = row.status === 0 ? 1 : 0;
    
    // 注意：不在这里检查最后一个超级管理员，因为前端只能看到当前页数据，不准确
    // 完全依赖后端检查，后端会返回明确的错误信息
    
    try {
      // 先恢复状态，避免 switch 已经改变
      row.status = originalStatus;
      
      await ElMessageBox.confirm(
        `确认要<strong>${
          originalStatus === 0 ? "启用" : "停用"
        }</strong><strong style='color:var(--el-color-primary)'>${
          row.username
        }</strong>用户吗?`,
        "系统提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
          dangerouslyUseHTMLString: true,
          draggable: true
        }
      );
      
      switchLoadMap.value[index] = Object.assign(
        {},
        switchLoadMap.value[index],
        {
          loading: true
        }
      );
      
      const response = await toggleUserStatus(row.id, newStatus);
      const { code, message: responseMessage } = response;
      
      if (code === 200) {
        row.status = newStatus;
        message("已成功修改用户状态", {
          type: "success"
        });
      } else {
        // 失败时恢复原状态（包括后端返回的业务错误，如：不能禁用最后一个超级管理员）
        row.status = originalStatus;
        // 显示后端返回的具体错误消息
        const errorMsg = responseMessage || "修改用户状态失败";
        message(errorMsg, {
          type: "error"
        });
      }
      
      switchLoadMap.value[index] = Object.assign(
        {},
        switchLoadMap.value[index],
        {
          loading: false
        }
      );
    } catch (error: any) {
      // 取消或失败时恢复原状态
      row.status = originalStatus;
      if (error !== "cancel") {
        // 尝试从错误响应中获取消息
        const errorMsg = error?.response?.data?.message || error?.message || "操作失败";
        message(errorMsg, {
          type: "error"
        });
      }
      switchLoadMap.value[index] = Object.assign(
        {},
        switchLoadMap.value[index],
        {
          loading: false
        }
      );
    }
  }

  function handleUpdate(row) {
    // 调试用，已注释
    // console.log(row);
  }

  async function handleDelete(row) {
    try {
      // 检查是否是最后一个超级管理员
      if (row.role === "SUPER_ADMIN") {
        const superAdminCount = dataList.value.filter(
          (item: any) => item.role === "SUPER_ADMIN" && item.status === 1
        ).length;
        if (superAdminCount <= 1) {
          message("不能删除最后一个超级管理员", { type: "warning" });
          return;
        }
      }
      
      const { code } = await deleteUser(row.id);
      if (code === 200) {
        message(`已成功删除用户 ${row.username}`, { type: "success" });
        onSearch();
      } else {
        message("删除用户失败", { type: "error" });
      }
    } catch (error: any) {
      message(error?.message || "删除用户失败", { type: "error" });
    }
  }

  function handleSizeChange(val: number) {
    pagination.pageSize = val;
    pagination.currentPage = 1;
    onSearch();
  }

  function handleCurrentChange(val: number) {
    pagination.currentPage = val;
    onSearch();
  }

  /** 当CheckBox选择项发生变化时会触发该事件 */
  function handleSelectionChange(val) {
    selectedNum.value = val.length;
    // 重置表格高度
    tableRef.value.setAdaptive();
  }

  /** 取消选择 */
  function onSelectionCancel() {
    selectedNum.value = 0;
    // 用于多选表格，清空用户的选择
    tableRef.value.getTableRef().clearSelection();
  }

  /** 批量删除 */
  async function onbatchDel() {
    // 返回当前选中的行
    const curSelected = tableRef.value.getTableRef().getSelectionRows();
    if (curSelected.length === 0) {
      message("请选择要删除的用户", { type: "warning" });
      return;
    }
    
    try {
      // 批量删除
      const deletePromises = curSelected.map((row: any) => deleteUser(row.id));
      await Promise.all(deletePromises);
      
      message(`已成功删除 ${curSelected.length} 个用户`, {
        type: "success"
      });
      tableRef.value.getTableRef().clearSelection();
      onSearch();
    } catch (error: any) {
      message(error?.message || "批量删除失败", { type: "error" });
    }
  }

  async function onSearch() {
    const searchStartTime = performance.now();
    loading.value = true;
    const loadingStartTime = performance.now();
    
    try {
      // 构建查询参数，包含 departmentId
      // 注意：如果 departmentId 是 0，表示未分配，应该传递 0 而不是 undefined
      // 如果 departmentId 是 undefined 或 null，则不传递该参数（显示所有用户）
      const params: any = {
        ...toRaw(form)
      };
      // 明确处理 departmentId：只有非 undefined 和非 null 时才传递
      if (form.departmentId !== undefined && form.departmentId !== null) {
        params.departmentId = form.departmentId;
      }
      const { code, data } = await getUserList(params);
      
      if (code === 0 && data) {
        dataList.value = data.list || [];
        pagination.total = data.total || 0;
        pagination.pageSize = data.pageSize || 10;
        pagination.currentPage = data.currentPage || 1;
      } else {
        // 如果返回错误，清空列表
        dataList.value = [];
        message("获取用户列表失败", {
          type: "error"
        });
      }
    } catch (error: any) {
      console.error("[用户列表查询] 请求失败", error);
      dataList.value = [];
      message(error?.message || "获取用户列表失败", {
        type: "error"
      }); 
    } finally {
      const totalTime = performance.now() - searchStartTime;
      // 只记录超过1秒的请求
      if (totalTime > 1000) {
        console.warn(`[性能警告] 用户列表查询总耗时: ${totalTime.toFixed(2)}ms`);
      }
      
      // 立即关闭 loading，不要延迟
      // 注意：如果请求很快（< 300ms），可以考虑添加最小显示时间避免闪烁
      // 但当前请求很慢（5秒），所以立即关闭即可
      loading.value = false;
    }
  }

  const resetForm = formEl => {
    if (!formEl) return;
    formEl.resetFields();
    onSearch();
  };

  async function openDialog(title = "新增", row?: FormItemProps) {
    // 检查是否是最后一个超级管理员
    let isLastSuperAdmin = false;
    if (title === "修改" && row?.role === "SUPER_ADMIN") {
      const superAdminCount = dataList.value.filter(
        (item: any) => item.role === "SUPER_ADMIN" && item.status === 1 && item.id !== row?.id
      ).length;
      isLastSuperAdmin = superAdminCount === 0;
    }
    
    // 如果是修改，保存原始数据用于比较是否发生变化
    let originalRoleIds: number[] = [];
    let roleIds: number[] = [];
    let originalUserData: Partial<FormItemProps> = {};
    if (title === "修改" && row?.id) {
      try {
        const roleResponse = await getRoleIds(row.id);
        if (roleResponse.code === 200 && roleResponse.data) {
          originalRoleIds = [...roleResponse.data]; // 保存原始角色ID列表（深拷贝）
          roleIds = roleResponse.data;
        }
      } catch (error) {
        console.error("获取用户角色失败", error);
      }
      // 保存原始用户信息（用于比较是否发生变化）
      originalUserData = {
        username: row?.username ?? "",
        nickname: row?.nickname ?? "",
        email: row?.email ?? "",
        phone: row?.phone ?? "",
        sex: row?.sex ?? "",
        role: row?.role ?? "USER",
        departmentId: row?.departmentId ?? 0,
        status: row?.status ?? 1,
        remark: row?.remark ?? ""
      };
    }
    
    addDialog({
      title: `${title}用户`,
      props: {
        formInline: {
          id: row?.id,
          title,
          nickname: row?.nickname ?? "",
          username: row?.username ?? "",
          password: row?.password ?? "",
          phone: row?.phone ?? "",
          email: row?.email ?? "",
          sex: row?.sex ?? "",
          role: row?.role ?? "USER",
          roleIds: roleIds, // 多角色ID列表
          departmentId: row?.departmentId ?? 0,
          status: row?.status ?? 1,
          remark: row?.remark ?? "",
          isLastSuperAdmin: isLastSuperAdmin // 传递是否是最后一个超级管理员
        }
      },
      width: "46%",
      draggable: true,
      fullscreen: deviceDetection(),
      fullscreenIcon: true,
      closeOnClickModal: false,
      sureBtnLoading: true, // 启用确定按钮的 loading 状态，防止重复提交
      contentRenderer: () => h(editForm, { ref: formRef, formInline: null }),
      beforeSure: async (done, { options, closeLoading }) => {
        const FormRef = formRef.value.getRef();
        // 从表单组件获取最新的表单数据，而不是从 options.props.formInline
        const formData = formRef.value.getFormData ? formRef.value.getFormData() : null;
        const curData = (formData || options.props.formInline) as FormItemProps;
        
        // 新增时验证密码必填
        if (title === "新增" && !curData.password) {
          closeLoading(); // 验证失败时关闭 loading
          message("用户密码为必填项", { type: "error" });
          return;
        }
        
        FormRef.validate(async (valid: boolean) => {
          if (valid) {
            try {
              if (title === "新增") {
                // 创建用户时，如果没有选择角色，使用默认角色 USER
                const defaultRole = curData.role || "USER";
                const response = await createUser({
                  username: curData.username,
                  nickname: curData.nickname || curData.username, // 如果没有填写昵称，使用用户名
                  email: curData.email,
                  phone: curData.phone,
                  password: curData.password,
                  role: defaultRole,
                  // 处理部门ID：如果 departmentId 是 undefined 或 null，则传递 0（未分配）
                  // 如果 departmentId 是 0 或其他数字，则正常传递
                  departmentId: curData.departmentId !== undefined && curData.departmentId !== null 
                    ? curData.departmentId 
                    : 0,
                  status: curData.status,
                  remark: curData.remark || ""
                });
                if (response.code === 200) {
                  // 创建用户成功后，分配角色（如果选择了角色）
                  if (curData.roleIds && curData.roleIds.length > 0) {
                    try {
                      // 获取新创建用户的ID（需要从响应中获取，或者重新查询）
                      // 这里先查询用户列表获取最新创建的用户的ID
                      const userListResponse = await getUserList({ keyword: curData.username, current: 1, size: 1 });
                      if (userListResponse.code === 200 && userListResponse.data) {
                        const userList = (userListResponse.data as any).records || (userListResponse.data as any).list || [];
                        if (userList.length > 0) {
                          const newUserId = userList[0].id;
                          await assignUserRoles(newUserId, curData.roleIds);
                        }
                      }
                    } catch (error) {
                      console.error("分配角色失败", error);
                      // 角色分配失败不影响用户创建成功
                    }
                  }
                  message(`成功创建用户 ${curData.username}`, {
                    type: "success"
                  });
                  done();
                  onSearch();
                } else {
                  closeLoading(); // 失败时关闭 loading
                  // 显示后端返回的具体错误信息
                  const errorMsg = response.message || "创建用户失败";
                  message(errorMsg, { type: "error" });
                }
              } else {
                // 修改用户
                // 准备更新数据
                const updateData: any = {
                  username: curData.username,
                  nickname: curData.nickname || curData.username, // 如果没有填写昵称，使用用户名
                  email: curData.email,
                  phone: curData.phone,
                  role: curData.role || "USER", // 保留主角色字段用于向后兼容
                  status: curData.status,
                  remark: curData.remark || ""
                };
                // 处理部门ID：如果 departmentId 是 undefined 或 null，则传递 0（未分配）
                // 如果 departmentId 是 0 或其他数字，则正常传递
                updateData.departmentId = curData.departmentId !== undefined && curData.departmentId !== null 
                  ? curData.departmentId 
                  : 0;
                
                // 比较用户信息是否发生变化
                const newUserData = {
                  username: curData.username,
                  nickname: curData.nickname || curData.username,
                  email: curData.email,
                  phone: curData.phone || "",
                  sex: curData.sex ?? "",
                  role: curData.role || "USER",
                  departmentId: curData.departmentId !== undefined && curData.departmentId !== null ? curData.departmentId : 0,
                  status: curData.status,
                  remark: curData.remark || ""
                };
                
                // 比较用户信息是否发生变化（排除密码，因为密码是可选更新的）
                const userInfoChanged = 
                  newUserData.username !== originalUserData.username ||
                  newUserData.nickname !== originalUserData.nickname ||
                  newUserData.email !== originalUserData.email ||
                  newUserData.phone !== (originalUserData.phone || "") ||
                  newUserData.sex !== (originalUserData.sex ?? "") ||
                  newUserData.role !== originalUserData.role ||
                  newUserData.departmentId !== originalUserData.departmentId ||
                  newUserData.status !== originalUserData.status ||
                  newUserData.remark !== (originalUserData.remark || "") ||
                  !!curData.password; // 如果提供了新密码，也算作变化
                
                // 比较角色是否发生变化（排序后比较，避免顺序不同导致的误判）
                const newRoleIds = [...(curData.roleIds || [])].sort((a, b) => a - b);
                const oldRoleIds = [...originalRoleIds].sort((a, b) => a - b);
                const roleChanged = JSON.stringify(newRoleIds) !== JSON.stringify(oldRoleIds);
                
                // 如果用户信息和角色都没有变化，提示用户
                if (!userInfoChanged && !roleChanged) {
                  closeLoading();
                  message("没有修改任何内容", { type: "warning" });
                  return;
                }
                
                // 如果用户信息发生变化，调用更新接口
                if (userInfoChanged) {
                  // 如果提供了新密码，则更新密码
                  if (curData.password) {
                    updateData.password = curData.password;
                  }
                  
                  const updateResponse = await updateUser(curData.id!, updateData);
                  if (updateResponse.code !== 200) {
                    closeLoading(); // 失败时关闭 loading
                    // 显示后端返回的具体错误信息
                    const errorMsg = updateResponse.message || "更新用户失败";
                    message(errorMsg, { type: "error" });
                    return;
                  }
                }
                
                // 如果角色发生变化，调用角色分配接口
                if (roleChanged) {
                  try {
                    console.log("角色发生变化，准备分配角色，用户ID:", curData.id, "原始角色IDs:", oldRoleIds, "新角色IDs:", newRoleIds);
                    await assignUserRoles(curData.id!, newRoleIds);
                    console.log("角色分配成功");
                  } catch (error: any) {
                    console.error("分配角色失败", error);
                    closeLoading();
                    message(error?.message || "分配角色失败", { type: "error" });
                    return;
                  }
                }
                
                // 根据实际变化情况显示提示信息
                if (userInfoChanged && roleChanged) {
                  message(`成功更新用户 ${curData.username} 的信息和角色`, {
                    type: "success"
                  });
                } else if (userInfoChanged) {
                  message(`成功更新用户 ${curData.username} 的信息`, {
                    type: "success"
                  });
                } else if (roleChanged) {
                  message(`成功更新用户 ${curData.username} 的角色`, {
                    type: "success"
                  });
                }
                
                done();
                onSearch();
              }
            } catch (error: any) {
              closeLoading(); // 异常时关闭 loading
              // 从错误响应中提取具体的错误信息
              const errorMsg = error?.response?.data?.message 
                || error?.message 
                || `${title === "新增" ? "创建" : "更新"}用户失败`;
              message(errorMsg, { type: "error" });
            }
          } else {
            closeLoading(); // 表单验证失败时关闭 loading
          }
        });
      }
    });
  }

  const cropRef = ref();
  /** 上传头像 */
  function handleUpload(row) {
    addDialog({
      title: "裁剪、上传头像",
      width: "40%",
      closeOnClickModal: false,
      fullscreen: deviceDetection(),
      sureBtnLoading: true, // 启用确定按钮的 loading 状态，防止重复提交
      contentRenderer: () =>
        h(ReCropperPreview, {
          ref: cropRef,
          imgSrc: row.avatar || userAvatar,
          onCropper: info => (avatarInfo.value = info)
        }),
      beforeSure: (done, { closeLoading }) => {
        try {
          // 调试用，已注释
          // console.log("裁剪后的图片信息：", avatarInfo.value);
          // 根据实际业务使用avatarInfo.value和row里的某些字段去调用上传头像接口即可
          done(); // 关闭弹框
          onSearch(); // 刷新表格数据
        } catch (error: any) {
          closeLoading(); // 异常时关闭 loading
          message(error?.message || "上传头像失败", { type: "error" });
        }
      },
      closeCallBack: () => cropRef.value.hidePopover()
    });
  }

  watch(
    pwdForm,
    ({ newPwd }) =>
      (curScore.value = isAllEmpty(newPwd) ? -1 : zxcvbn(newPwd).score)
  );

  /** 重置密码 */
  function handleReset(row) {
    addDialog({
      title: `重置 ${row.username} 用户的密码`,
      width: "30%",
      draggable: true,
      closeOnClickModal: false,
      fullscreen: deviceDetection(),
      sureBtnLoading: true, // 启用确定按钮的 loading 状态，防止重复提交
      contentRenderer: () => (
        <>
          <ElForm ref={ruleFormRef} model={pwdForm}>
            <ElFormItem
              prop="newPwd"
              rules={[
                {
                  required: true,
                  message: "请输入新密码",
                  trigger: "blur"
                }
              ]}
            >
              <ElInput
                clearable
                show-password
                type="password"
                v-model={pwdForm.newPwd}
                placeholder="请输入新密码"
              />
            </ElFormItem>
          </ElForm>
          <div class="my-4 flex">
            {pwdProgress.map(({ color, text }, idx) => (
              <div
                class="w-[19vw]"
                style={{ marginLeft: idx !== 0 ? "4px" : 0 }}
              >
                <ElProgress
                  striped
                  striped-flow
                  duration={curScore.value === idx ? 6 : 0}
                  percentage={curScore.value >= idx ? 100 : 0}
                  color={color}
                  stroke-width={10}
                  show-text={false}
                />
                <p
                  class="text-center"
                  style={{ color: curScore.value === idx ? color : "" }}
                >
                  {text}
                </p>
              </div>
            ))}
          </div>
        </>
      ),
      closeCallBack: () => (pwdForm.newPwd = ""),
      beforeSure: async (done, { closeLoading }) => {
        ruleFormRef.value.validate(async (valid: boolean) => {
          if (valid) {
            try {
              const { code } = await updateUser(row.id, {
                username: row.username,
                nickname: row.nickname,
                email: row.email,
                phone: row.phone,
                role: row.role || "USER",
                status: row.status,
                password: pwdForm.newPwd
              });
              if (code === 200) {
                message(`已成功重置 ${row.username} 用户的密码`, {
                  type: "success"
                });
                done();
                onSearch();
              } else {
                closeLoading(); // 失败时关闭 loading
                message("重置密码失败", { type: "error" });
              }
            } catch (error: any) {
              closeLoading(); // 异常时关闭 loading
              message(error?.message || "重置密码失败", { type: "error" });
            }
          } else {
            closeLoading(); // 表单验证失败时关闭 loading
          }
        });
      }
    });
  }

  /** 分配角色 */
  async function handleRole(row) {
    // 选中的角色列表
    const ids = (await getRoleIds(row.id)).data ?? [];
    addDialog({
      title: `分配 ${row.username} 用户的角色`,
      props: {
        formInline: {
          username: row?.username ?? "",
          nickname: row?.nickname ?? "",
          roleOptions: roleOptions.value ?? [],
          ids
        }
      },
      width: "400px",
      draggable: true,
      fullscreen: deviceDetection(),
      fullscreenIcon: true,
      closeOnClickModal: false,
      sureBtnLoading: true, // 启用确定按钮的 loading 状态，防止重复提交
      contentRenderer: () => h(roleForm, { ref: roleFormRef, formInline: null }),
      beforeSure: async (done, { options, closeLoading }) => {
        try {
          // 从表单组件获取最新的表单数据
          const formData = roleFormRef.value?.getFormData ? roleFormRef.value.getFormData() : null;
          const curData = (formData || options.props.formInline) as RoleFormItemProps;
          
          console.log("分配角色，用户ID:", row.id, "角色IDs:", curData.ids);
          // 调用API保存用户角色分配（支持多角色）
          await assignUserRoles(row.id, curData.ids ?? []);
          console.log("角色分配成功");
          message(`已成功为 ${row.username} 分配角色`, { type: "success" });
          done(); // 关闭弹框
          onSearch(); // 刷新表格数据
        } catch (error: any) {
          console.error("分配角色失败", error);
          closeLoading(); // 异常时关闭 loading
          message(error?.message || "分配角色失败", { type: "error" });
        }
      }
    });
  }

  onMounted(async () => {
    onSearch();

    // 角色列表（从数据库获取，包含id和name）
    const roleResponse = await getAllRolesFromDB();
    if (roleResponse.code === 200 && roleResponse.data) {
      roleOptions.value = roleResponse.data.map((role: any) => ({
        id: role.id,
        name: role.name
      }));
    } else {
      roleOptions.value = [];
    }
  });

  return {
    form,
    loading,
    columns,
    dataList,
    selectedNum,
    pagination,
    buttonClass,
    deviceDetection,
    onSearch,
    resetForm,
    onbatchDel,
    openDialog,
    handleUpdate,
    handleDelete,
    handleUpload,
    handleReset,
    handleSizeChange,
    onSelectionCancel,
    handleCurrentChange,
    handleSelectionChange
  };
}
