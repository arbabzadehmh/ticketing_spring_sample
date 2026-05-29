let firstLoad = true;

let currentEditingPermissionId = null;

function loadPermissions(page = 0) {
    const container = document.getElementById('permissions-table-container');
    const pageSizeElement = document.getElementById('pageSize');
    const searchPermissionNameElement = document.getElementById('searchPermissionName');

    if (!container || !pageSizeElement) {
        console.log('%cPermissions table not found, skipping loadPermissions()', 'color: orange;');
        return;
    }

    const size = parseInt(pageSizeElement.value, 10);
    const searchPermissionName = searchPermissionNameElement ? searchPermissionNameElement.value.trim() : '';

    if (firstLoad) {
        firstLoad = false;

        console.log("web controller / fetch")

        let url = `/permissions?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}&fragment=true`;


        fetch(url)
            .then(response => {
                if (!response.ok) throw new Error('خطا در دریافت داده‌ها');
                return response.text();
            })
            .then(html => {
                container.innerHTML = html;

                document.getElementById('pageSize').value = size;
                const newSearchPermissionNameElement = document.getElementById('searchPermissionName');
                if (newSearchPermissionNameElement) {
                    newSearchPermissionNameElement.value = searchPermissionName;
                }

            })
            .catch(error => showToast('danger', error.message || 'خطا در دریافت داده‌ها'));

        return;
    }

    console.log("rest controller / fetch")
    let apiUrl = `/rest/permissions/get-all?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}`;

    if (searchPermissionName) {
        apiUrl += `&searchPermissionName=${encodeURIComponent(searchPermissionName)}`;
    }

    const currentPage = page;

    fetch(apiUrl)
        .then(response => {
            if (!response.ok) throw new Error('خطا در دریافت داده‌ها');
            return response.json();
        })
        .then(data => {
            renderPermissionsTable(data.content);
            renderPagination(data);

            document.getElementById('pageSize').value = size;

            // اضافه: بعد از رندر pagination، دوباره روی همان صفحه بایست
            const paginationContainer = document.querySelector(".pagination");
            const activeLink = paginationContainer.querySelector(`.page-link[data-page='${currentPage}']`);
            if (activeLink) activeLink.parentElement.classList.add('active');
        })
        .catch(error => showToast('danger', error.message || 'خطا در دریافت داده‌ها'));
}

// -------------------- Render Table --------------------
function renderPermissionsTable(permissions) {
    const tableBody = document.querySelector("#permissions-table tbody");
    tableBody.innerHTML = '';

    permissions.forEach(p => {
        const row = document.createElement('tr');

        row.innerHTML = `
            <td>${p.permissionName}</td>
            <td>
                <button class="btn btn-sm btn-warning btn-edit"
                    data-id="${p.id}"
                    data-name='${p.permissionName}'
                    data-version="${p.version}">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-danger btn-delete" data-id="${p.id}">
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

// ----------------------------------------------------
async function handlePermissionSubmit(e) {
    e.preventDefault();
    clearValidationErrors();

    const id = document.getElementById('permissionId').value;
    const permission = {permissionName: document.getElementById('permissionName').value.trim()};

    if (id) {
        permission.version = document.getElementById('permissionVersion').value;
    }

    const url = id ? `/rest/permissions/${id}` : '/rest/permissions';
    const method = id ? 'PUT' : 'POST';

    try {
        const response = await secureFetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(permission)
        });

        const data = await handleResponse(response, id ? 'edit' : 'create');

        if (id) {
            await secureFetch(`/rest/permissions/${id}/edit-stop`, {
                method: 'POST'
            });
        }

        currentEditingPermissionId = null;

        bootstrap.Modal.getInstance(document.getElementById('permissionModal')).hide();
        showToast('success', data.message || (id ? 'دسترسی ویرایش شد' : 'دسترسی ثبت شد'));
        loadPermissions();
    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Permission save error:', error);
            showToast('danger', error.message || 'خطا در ذخیره دسترسی');
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

// -------------------------------------------------------
async function handlePermissionDelete(e) {
    const btn = e.target.closest('.btn-danger');
    const confirmTextElem = document.getElementById('deleteConfirmText');
    const confirmText = confirmTextElem ? confirmTextElem.textContent.trim() : "Are you sure?";
    if (!confirm(confirmText)) return;

    const id = btn.dataset.id;

    try {
        const response = await secureFetch(`/rest/permissions/${id}`, {
            method: 'DELETE'
        });
        const data = await handleResponse(response, 'delete');
        showToast('success', data.message || 'دسترسی حذف شد');
        loadPermissions();
    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Permission deletion error:', error);
            showToast('danger', error.message || 'خطا در حذف دسترسی');
        }
    }
}

// ---------------------------------------------------------
function getCsrfToken() {
    return document.querySelector("meta[name='_csrf']").content;
}

function getCsrfHeader() {
    return document.querySelector("meta[name='_csrf_header']").content;
}

async function secureFetch(url, options = {}) {

    options.headers = {
        ...(options.headers || {}),
        [getCsrfHeader()]: getCsrfToken()
    };

    return fetch(url, options);
}

// ---------------------------------------------------------
function debounce(fn, delay) {
    let timeoutId;
    return function (...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => fn.apply(this, args), delay);
    };
}

// ----------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
    // بارگذاری اولیه
    loadPermissions();

    const form = document.getElementById('permissionForm');
    if (form) {
        form.addEventListener('submit', handlePermissionSubmit);
    }

    const modalTitle = document.getElementById('permissionModalLabel');
    const permissionModal = document.getElementById('permissionModal');

    // --- مدیریت مودال ---
    if (permissionModal) {
        // باز شدن مودال
        permissionModal.addEventListener('show.bs.modal', (event) => {
            const isAdd = event.relatedTarget && event.relatedTarget.classList.contains('btn-add');
            if (isAdd) {
                modalTitle.textContent = modalTitle.dataset.titleAdd;
                document.getElementById('permissionId').value = '';
                document.getElementById('permissionName').value = '';
            }
        });

        // بستن مودال
        permissionModal.addEventListener('hidden.bs.modal', async () => {

            const id = currentEditingPermissionId;

            currentEditingPermissionId = null;

            if (!id) return;

            try {
                await secureFetch(`/rest/permissions/${id}/edit-stop`, {
                    method: 'POST'
                });
            } catch (e) {
                console.error('edit-stop error', e);
            }

            modalTitle.textContent = modalTitle.dataset.titleAdd;

            const focusedElement = permissionModal.querySelector(':focus');
            if (focusedElement) focusedElement.blur();
        });
    }

    // --- delegation کلیک‌ها ---
    document.body.addEventListener('click', async (e) => {
        const btnDelete = e.target.closest('.btn-delete, .btn-danger');
        if (btnDelete) {
            handlePermissionDelete(e);
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

            const permissionId = btnEdit.dataset.id;

            try {

                const lockResponse = await secureFetch(`/rest/permissions/${permissionId}/edit-start`, {
                    method: 'POST'
                });

                let lockData;
                let rawText = await lockResponse.text();

                try {
                    lockData = JSON.parse(rawText);
                } catch (e) {
                    lockData = { error: rawText };
                }

                if (!lockResponse.ok) {

                    btnEdit.dataset.loading = 'false';

                    showToast(
                        'danger',
                        lockData.error || 'این بخش توسط کاربر دیگری در حال ویرایش است !'
                    );

                    return;
                }

                currentEditingPermissionId = permissionId;

                modalTitle.textContent =
                    modalTitle.dataset.titleEdit;

                document.getElementById('permissionId').value = btnEdit.dataset.id;
                document.getElementById('permissionName').value = btnEdit.dataset.name;
                document.getElementById('permissionVersion').value = btnEdit.dataset.version || '';

                // فقط همین
                const modalInstance =
                    bootstrap.Modal.getOrCreateInstance(permissionModal);

                modalInstance.show();

            } catch (error) {

                console.error('edit-start error:', error);

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
            const page = pageAttr != null
                ? parseInt(pageAttr, 10)
                : (parseInt(pageLink.textContent, 10) - 1);

            if (!isNaN(page)) loadPermissions(page);
        }
    });

    // --- سرچ و تغییر سایز صفحه ---
    const debouncedLoad = debounce(() => loadPermissions(0), 700);

    document.body.addEventListener('input', (e) => {
        if (e.target.id === 'searchPermissionName') {
            debouncedLoad();
        }
    });

    document.body.addEventListener('change', (e) => {
        if (e.target.id === 'pageSize') {
            loadPermissions(0);
        }
    });
});


