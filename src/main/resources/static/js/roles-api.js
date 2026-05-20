let firstLoad = true;

let currentEditingRoleName = null;

// -------------------- Load Roles --------------------
function loadRoles(page = 0) {
    const container = document.getElementById('roles-table-container');
    const pageSizeElement = document.getElementById('pageSize');
    const searchRoleNameElement = document.getElementById('searchRoleName');

    if (!container || !pageSizeElement) {
        console.log('%cRoles table not found, skipping loadRoles()', 'color: orange;');
        return;
    }

    const size = parseInt(pageSizeElement.value, 10);
    const searchRoleName = searchRoleNameElement ? searchRoleNameElement.value.trim() : '';

    // --- بار اول: از WebController (fragment) ---
    if (firstLoad) {
        firstLoad = false;

        console.log("web controller / fetch")

        const url = `/roles?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}&fragment=true`;
        fetch(url)
            .then(res => {
                if (!res.ok) throw new Error('خطا در دریافت داده‌ها');
                return res.text();
            })
            .then(html => {
                container.innerHTML = html;

                // برگردوندن انتخاب‌های کاربر
                const ps = document.getElementById('pageSize');
                if (ps) ps.value = size;

                const newSearchRoleNameElement = document.getElementById('searchRoleName');
                if (newSearchRoleNameElement) newSearchRoleNameElement.value = searchRoleName;

                // REMOVED: هیچ initXYZ اینجا صدا زده نمی‌شه. (delegation داریم)
                // اگر فرم‌های داخل مودال‌ها وجود دارن، submitشون رو یکبار bind می‌کنیم
                // const editForm = document.getElementById('roleEditForm');
                // if (editForm && !editForm.dataset.bound) {
                //     editForm.addEventListener('submit', handleEditRoleSubmit);
                //     editForm.dataset.bound = 'true'; // CHANGED: جلوگیری از دوباره بایند شدن
                // }

            })
            .catch(err => showToast('danger', err.message || 'خطا در دریافت داده‌ها'));
        return;
    }

    // --- دفعات بعد: RestController (JSON) ---
    console.log("rest controller / fetch")

    let apiUrl = `/rest/roles/get-all?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}`;
    if (searchRoleName) apiUrl += `&searchRoleName=${encodeURIComponent(searchRoleName)}`;

    // ذخیره صفحه جاری
    const currentPage = page;

    fetch(apiUrl)
        .then(res => {
            if (!res.ok) throw new Error('خطا در دریافت داده‌ها');
            return res.json();
        })
        .then(data => {
            renderRolesTable(data.content);
            renderPagination(data, currentPage);

            const ps = document.getElementById('pageSize');
            if (ps) ps.value = size;

            // اضافه: بعد از رندر pagination، دوباره روی همان صفحه بایست
            const paginationContainer = document.querySelector(".pagination");
            const activeLink = paginationContainer.querySelector(`.page-link[data-page='${currentPage}']`);
            if (activeLink) activeLink.parentElement.classList.add('active');
        })
        .catch(err => showToast('danger', err.message || 'خطا در دریافت داده‌ها'));
}

// -------------------- Render Table --------------------
function renderRolesTable(roles) {
    const tableBody = document.querySelector("#roles-table tbody");
    if (!tableBody) return;

    tableBody.innerHTML = '';

    roles.forEach(r => {
        const row = document.createElement('tr');

        const permissions = r?.permissionSet
            ? r.permissionSet.map(p => p.permissionName).join(",")
            : '';

        row.innerHTML = `
      <td>${r.name}</td>
      <td>
        <button class="btn btn-sm btn-warning btn-edit"
          data-id="${r.name}"
          data-permissions='${permissions}'
          data-version="${r.version}">
          <i class="fas fa-edit"></i>
        </button>
        <button class="btn btn-sm btn-danger btn-delete"
          data-id="${r.name}">
          <i class="fas fa-trash"></i>
        </button>
      </td>
    `;
        tableBody.appendChild(row);
    });
}

// -------------------- Render Pagination --------------------
function renderPagination(data, currentPage = 0) { // اضافه کردن currentPage
    const paginationContainer = document.querySelector(".pagination");
    if (!paginationContainer) return;

    paginationContainer.innerHTML = '';

    for (let i = 0; i < data.totalPages; i++) {
        const li = document.createElement('li');
        li.className = 'page-item' + (i === currentPage ? ' active' : ''); // استفاده از currentPage
        const a = document.createElement('a');
        a.className = 'page-link';
        a.textContent = (i + 1);
        a.href = '#';
        a.dataset.page = i; // dataset-page برای delegation
        li.appendChild(a);
        paginationContainer.appendChild(li);
    }
}

// -------------------- Create Role --------------------
async function handleCreateRoleSubmit(e) {
    e.preventDefault();
    clearValidationErrors();

    const role = {
        name: document.getElementById('createName').value.trim(),
        permissionSet: Array.from(document.querySelectorAll('#permissionsDropdownMenu .permission-checkbox:checked'))
            .map(cb => ({ permissionName: cb.value }))
    };

    try {
        const response = await fetch('/rest/roles', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(role)
        });

        const data = await handleResponse(response, 'create');

        bootstrap.Modal.getInstance(document.getElementById('roleCreateModal'))?.hide();
        showToast('success', data.message || 'نقش با موفقیت ثبت شد');
        loadRoles();

    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Role creation error:', error);
            showToast('danger', error.message || 'خطا در ثبت نقش');
        }
    }
}

// -------------------- Edit Role --------------------
async function handleEditRoleSubmit(e) {
    e.preventDefault();
    clearValidationErrors();

    const id = document.getElementById('editRoleName').value;

    const role = {
        name: document.getElementById('editName').value.trim(),
        version : document.getElementById('editRoleVersion').value,
        permissionSet: Array.from(document.querySelectorAll('#editPermissionsDropdownMenu .permission-checkbox:checked'))
            .map(cb => ({ permissionName: cb.value }))
    };

    try {
        const response = await fetch(`/rest/roles/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(role)
        });

        const data = await handleResponse(response, 'edit');

        try {
            await fetch(`/rest/roles/${id}/edit-stop`, {
                method: 'POST'
            });
        } catch (e) {
            console.error('unlock error', e);
        }

        currentEditingRoleName = null;

        bootstrap.Modal.getInstance(document.getElementById('roleEditModal'))?.hide();
        showToast('success', data.message || 'نقش با موفقیت ویرایش شد');
        loadRoles();

    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Role edition error:', error);
            showToast('danger', error.message || 'خطا در ویرایش نقش');
        }
    }
}

// -------------------- Handle Server Response --------------------
async function handleResponse(response, mode) {
    const data = await response.json();

    if (!response.ok) {
        if (response.status === 400) {
            displayValidationErrors(data, mode);
            throw new Error('Validation errors');
        }
        const errorMessage = data.error || 'خطای ناشناخته در سرور';
        showToast('danger', errorMessage);
        throw new Error(errorMessage);
    }
    return data;
}

// -------------------- Load permissions into modals --------------------
function loadPermissionsForCreateModal() {
    fetch('/rest/permissions')
        .then(response => {
            if (!response.ok) throw new Error('خطا در دریافت نقش‌ها');
            return response.json();
        })
        .then(permissions => {
            const menu = document.getElementById('permissionsDropdownMenu');
            if (!menu) return;
            menu.innerHTML = '';

            permissions.forEach(permissionName => {
                const li = document.createElement('li');
                li.innerHTML = `
          <div class="form-check">
            <input class="form-check-input permission-checkbox" type="checkbox" value="${permissionName}" id="role-${permissionName}">
            <label class="form-check-label" for="permission-${permissionName}">
              ${permissionName}
            </label>
          </div>
        `;
                menu.appendChild(li);
            });
        })
        .catch(err => console.error('Error loading permissions:', err));
}

const roleCreateModal = document.getElementById('roleCreateModal');
if (roleCreateModal && !roleCreateModal.dataset.bound) { // CHANGED: جلوگیری از دوباره بایند
    roleCreateModal.addEventListener('show.bs.modal', () => {
        loadPermissionsForCreateModal();
    });
    roleCreateModal.dataset.bound = 'true';
}

function loadPermissionsForEditModal(selectedPermissions = []) {
    fetch('/rest/permissions')
        .then(response => response.json())
        .then(data => {
            const menu = document.getElementById('editPermissionsDropdownMenu');
            if (!menu) return;
            menu.innerHTML = '';

            data.forEach(permission => {
                const li = document.createElement('div');
                li.className = 'form-check';
                li.innerHTML = `
          <input class="form-check-input permission-checkbox" type="checkbox" value="${permission}" id="edit-permission-${permission}"
            ${selectedPermissions.includes(permission) ? 'checked' : ''}>
          <label class="form-check-label" for="edit-permission-${permission}">${permission}</label>
        `;
                menu.appendChild(li);
            });
        });
}

// -------------------- Validation helpers --------------------
function displayValidationErrors(errors, mode) {
    clearValidationErrors();

    for (const field in errors) {
        const cleanField = field.includes('.') ? field.split('.').pop() : field;

        let input = null;
        if (mode === 'edit') input = document.getElementById('edit' + capitalize(cleanField));
        else if (mode === 'create') input = document.getElementById('create' + capitalize(cleanField));
        if (!input) input = document.getElementById(cleanField);

        if (input) {
            input.classList.add('is-invalid');
            const errorDiv = document.createElement('div');
            errorDiv.className = 'invalid-feedback';
            errorDiv.textContent = errors[field];
            input.parentNode.appendChild(errorDiv);
        }
    }
}

// --------------------------------------------------
function clearValidationErrors() {
    document.querySelectorAll('.is-invalid').forEach(i => i.classList.remove('is-invalid'));
    document.querySelectorAll('.invalid-feedback').forEach(f => f.remove());
}

function capitalize(str) { return str.charAt(0).toUpperCase() + str.slice(1); }

// -------------------- Delete Role --------------------
async function handleRoleDelete(e) {
    const btn = e.target.closest('.btn-delete, .btn-danger');
    if (!btn) return;
    const confirmTextElem = document.getElementById('deleteConfirmText');
    const confirmText = confirmTextElem ? confirmTextElem.textContent.trim() : "Are you sure?";
    if (!confirm(confirmText)) return;

    const id = btn.dataset.id;
    try {
        const response = await fetch(`/rest/roles/${id}`, { method: 'DELETE' });
        const data = await handleResponse(response, 'delete');
        showToast('success', data.message || 'نقش با موفقیت حذف شد');
        loadRoles();
    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Role deletion error:', error);
            showToast('danger', error.message || 'خطا در حذف نقش');
        }
    }
}

// -------------------- Debounce & Search --------------------
function debounce(fn, delay) {
    let timeoutId;
    return function (...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => fn.apply(this, args), delay);
    };
}

// -------------------- Global Delegation (single place) --------------------
document.addEventListener('DOMContentLoaded', () => {
    loadRoles();

    // فرم‌ها
    const createForm = document.getElementById('roleCreateForm');
    if (createForm && !createForm.dataset.bound) {
        createForm.addEventListener('submit', handleCreateRoleSubmit);
        createForm.dataset.bound = 'true';
    }

    const editForm = document.getElementById('roleEditForm');
    if (editForm && !editForm.dataset.bound) {
        editForm.addEventListener('submit', handleEditRoleSubmit);
        editForm.dataset.bound = 'true';
    }

    // کلیک‌ها (delegation)
    document.body.addEventListener('click', async e => {
        const btnDelete = e.target.closest('.btn-delete, .btn-danger');
        if (btnDelete) {
            handleRoleDelete(e);
            return;
        }

        const btnEdit = e.target.closest('.btn-edit');

        if (btnEdit) {

            e.preventDefault();
            e.stopPropagation();

            // جلوگیری از چند کلیک سریع
            if (btnEdit.dataset.loading === 'true') {
                return;
            }

            btnEdit.dataset.loading = 'true';

            const roleName = btnEdit.dataset.id;

            try {

                const lockResponse = await fetch(
                    `/rest/roles/${roleName}/edit-start`,
                    {
                        method: 'POST'
                    }
                );

                let lockData;
                const rawText = await lockResponse.text();

                try {
                    lockData = JSON.parse(rawText);
                } catch {
                    lockData = { error: rawText };
                }

                if (!lockResponse.ok) {

                    showToast(
                        'danger',
                        lockData.error || 'این نقش در حال ویرایش است'
                    );

                    return;
                }

                currentEditingRoleName = roleName;

                const row = btnEdit.closest('tr');

                document.getElementById('editRoleName').value =
                    roleName || '';

                document.getElementById('editName').value =
                    row ? row.cells[0].textContent.trim() : '';

                document.getElementById('editRoleVersion').value =
                    btnEdit.dataset.version || '';

                const selected = btnEdit.dataset.permissions
                    ? btnEdit.dataset.permissions.split(',')
                    : [];

                loadPermissionsForEditModal(selected);

                bootstrap.Modal
                    .getOrCreateInstance(
                        document.getElementById('roleEditModal')
                    )
                    .show();

            } catch (error) {

                console.error('role edit-start error:', error);

                showToast(
                    'danger',
                    'خطا در بررسی قفل ویرایش'
                );

            } finally {

                btnEdit.dataset.loading = 'false';
            }

            return;
        }

        const pageLink = e.target.closest('.page-link');
        if (pageLink) {
            e.preventDefault();
            const pageAttr = pageLink.dataset.page;
            const page = pageAttr != null ? parseInt(pageAttr, 10) : (parseInt(pageLink.textContent, 10) - 1);
            if (!isNaN(page)) loadRoles(page);
            return;
        }
    });

    const debouncedLoad = debounce(() => loadRoles(0), 700);
    document.body.addEventListener('input', e => {
        if (e.target.id === 'searchRoleName') debouncedLoad();
    });
    document.body.addEventListener('change', e => {
        if (e.target.id === 'pageSize') loadRoles(0);
    });


    const roleEditModal = document.getElementById('roleEditModal');

    if (roleEditModal) {

        roleEditModal.addEventListener(
            'hidden.bs.modal',
            async () => {

                const roleName = currentEditingRoleName;

                currentEditingRoleName = null;

                if (!roleName) return;

                try {

                    await fetch(
                        `/rest/roles/${roleName}/edit-stop`,
                        {
                            method: 'POST'
                        }
                    );

                } catch (e) {

                    console.error(
                        'role edit-stop error',
                        e
                    );
                }

                document.getElementById('roleEditForm')?.reset();

                clearValidationErrors();
            }
        );
    }

});