function loadRoles(page = 0) {
    // اگر table وجود نداشت، از تابع خارج شو
    const container = document.getElementById('roles-table-container');
    const pageSizeElement = document.getElementById('pageSize');

    if (!container || !pageSizeElement) {
        console.log('%cRoles table not found, skipping loadRoles()', 'color: orange;');
        return;
    }

    const size = parseInt(pageSizeElement.value, 10);

    const url = `/roles?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}&fragment=true`;

    fetch(url)
        .then(response => {
            if (!response.ok) throw new Error('خطا در دریافت داده‌ها');
            return response.text();
        })
        .then(html => {
            container.innerHTML = html;

            // بازگرداندن مقادیر انتخابی dropdown ها بعد از رندر مجدد
            document.getElementById('pageSize').value = size;

            initPagination();
            initSortAndPageSize();
            initDeleteButtons();
            initEditButtons();

            // ثبت دوباره رویداد submit برای فرم edit
            const editForm = document.getElementById('roleEditForm');
            if (editForm) {
                editForm.addEventListener('submit', handleEditRoleSubmit);
            }
        })
        .catch(error => showToast('danger', error.message || 'خطا در دریافت داده‌ها'));
}

// -------------------- Create Role --------------------
async function handleCreateRoleSubmit(e) {
    e.preventDefault();
    clearValidationErrors();

    const role = {
        name: document.getElementById('createName').value.trim(),
        permissionSet: Array.from(document.querySelectorAll('#permissionsDropdownMenu .permission-checkbox:checked'))
            .map(cb => ({permissionName: cb.value}))
    };



    try {
        const response = await fetch('/roles', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(role)
        });

        const data = await handleResponse(response, 'create');

        bootstrap.Modal.getInstance(document.getElementById('roleCreateModal')).hide();
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
        name: document.getElementById('editName').value.trim()
    };

    role.permissionSet = Array.from(document.querySelectorAll('#editPermissionsDropdownMenu .permission-checkbox:checked'))
        .map(cb => ({permissionName: cb.value}));

    try {
        const response = await fetch(`/roles/${id}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(role)
        });

        const data = await handleResponse(response, 'edit');

        bootstrap.Modal.getInstance(document.getElementById('roleEditModal')).hide();
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
            // خطاهای اعتبارسنجی
            displayValidationErrors(data, mode);
            throw new Error('Validation errors');
        }

        // سایر خطاها (۵۰۰ یا ...)، پیام در فیلد error است
        const errorMessage = data.error || 'خطای ناشناخته در سرور';
        showToast('danger', errorMessage);
        throw new Error(errorMessage);
    }

    // موفقیت (پیام در فیلد message است)
    return data;
}

// -----------------------------------------------------------
function loadPermissionsForCreateModal() {
    fetch('/rest/permissions')
        .then(response => {
            if (!response.ok) throw new Error('خطا در دریافت نقش‌ها');
            return response.json();
        })
        .then(permissions => {
            const menu = document.getElementById('permissionsDropdownMenu');
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
        .catch(err => {
            console.error('Error loading permissions:', err);
        });
}


const roleCreateModal = document.getElementById('roleCreateModal');
if (roleCreateModal) {
    roleCreateModal.addEventListener('show.bs.modal', () => {
        loadPermissionsForCreateModal();
    });
}

// --------------------------------------------------------------
function loadPermissionsForEditModal(selectedPermissions = []) {
    fetch('/rest/permissions')
        .then(response => response.json())
        .then(data => {
            const menu = document.getElementById('editPermissionsDropdownMenu');
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


// -------------------------------------------------------------
function displayValidationErrors(errors, mode) {
    clearValidationErrors();

    for (const field in errors) {
        const cleanField = field.includes('.') ? field.split('.').pop() : field;

        let input = null;
        if (mode === 'edit') {
            input = document.getElementById('edit' + capitalize(cleanField));
        } else if (mode === 'create') {
            input = document.getElementById('create' + capitalize(cleanField));
        }

        // حالت fallback
        if (!input) {
            input = document.getElementById(cleanField);
        }

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

function capitalize(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
}

// -------------------- Delete Role --------------------
async function handleRoleDelete(e) {
    const btn = e.target.closest('.btn-danger');
    const confirmText = btn.dataset.confirmText;
    if (!confirm(confirmText)) return;

    const id = btn.dataset.id;
    try {
        const response = await fetch(`/roles/${id}`, {
            method: 'DELETE',
        });

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

// -------------------- Init Modals --------------------
function initEditButtons() {
    document.querySelectorAll('.btn-edit').forEach(btn => {
        btn.addEventListener('click', () => {
            const row = btn.closest('tr');
            document.getElementById('editRoleName').value = btn.dataset.id;
            document.getElementById('editName').value = row.cells[0].textContent.trim();

            loadPermissionsForEditModal(btn.dataset.permissions ? btn.dataset.permissions.split(',') : []);

            new bootstrap.Modal(document.getElementById('roleEditModal')).show();
        });
    });
}

// -------------------------------------------------------
function initPagination() {
    document.querySelectorAll('.page-link').forEach(link => {
        link.addEventListener('click', e => {
            e.preventDefault();
            const page = link.dataset.page;
            loadRoles(page); // اینجا صفحه را به تابع بده
        });
    });
}

// --------------------------------------------------
function initSortAndPageSize() {
    const pageSize = document.getElementById('pageSize');
    if (pageSize) pageSize.addEventListener('change', () => loadRoles(0));
}


// -------------------------------------------------
function initDeleteButtons() {
    document.querySelectorAll('.btn-danger').forEach(btn => btn.addEventListener('click', handleRoleDelete));
}

// -------------------- DOMContentLoaded --------------------
document.addEventListener('DOMContentLoaded', () => {
    loadRoles();

    const createForm = document.getElementById('roleCreateForm');
    if (createForm) createForm.addEventListener('submit', handleCreateRoleSubmit);

    const editForm = document.getElementById('roleEditForm');
    if (editForm) editForm.addEventListener('submit', handleEditRoleSubmit);

    initPagination();
    initSortAndPageSize();
    initDeleteButtons();
});
