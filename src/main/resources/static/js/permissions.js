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

    let url = `/permissions?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}&fragment=true`;

    if (searchPermissionName) {
        url += `&searchPermissionName=${encodeURIComponent(searchPermissionName)}`;
    }

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

            initPagination();
            initSortAndPageSize();
            initDeleteButtons();
            initEditButtons();
            initAddButtons();
            initSearchInput();
        })
        .catch(error => showToast('danger', error.message || 'خطا در دریافت داده‌ها'));
}

// ----------------------------------------------------
async function handlePermissionSubmit(e) {
    e.preventDefault();
    clearValidationErrors();

    const id = document.getElementById('permissionId').value;
    console.log("Permission ID on submit:", id);
    const permission = {permissionName: document.getElementById('permissionName').value.trim()};

    const url = id ? `/permissions/${id}` : '/permissions';
    const method = id ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, {
            method,
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(permission)
        });

        const data = await handleResponse(response, id ? 'edit' : 'create');

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
function initEditButtons() {
    document.querySelectorAll('.btn-edit').forEach(btn => {
        btn.addEventListener('click', () => {
            const modalTitle = document.querySelector('#permissionModalLabel');
            modalTitle.textContent = modalTitle.dataset.titleEdit;
            document.getElementById('permissionId').value = btn.dataset.id;
            document.getElementById('permissionName').value = btn.dataset.name;
            new bootstrap.Modal(document.getElementById('permissionModal')).show();
        });
    });
}

// -------------------------------------------------------
function initAddButtons() {
    const addPermissionButton = document.querySelector('[data-bs-target="#permissionModal"]');
    if (addPermissionButton) {
        addPermissionButton.addEventListener('click', () => {
            const modalTitle = document.getElementById('permissionModalLabel');
            modalTitle.textContent = modalTitle.dataset.titleAdd; // پیش‌فرض عنوان افزودن
            document.getElementById('permissionId').value = '';  //  حتما خالی کن id رو
            document.getElementById('permissionName').value = '';
        });
    }
}

// -------------------------------------------------------
async function handlePermissionDelete(e) {
    const btn = e.target.closest('.btn-danger');
    const confirmText = btn.dataset.confirmText;
    if (!confirm(confirmText)) return;

    const id = btn.dataset.id;

    try {
        const response = await fetch(`/permissions/${id}`, {method: 'DELETE'});
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

// -----------------------------------------------------
function initDeleteButtons() {
    document.querySelectorAll('.btn-danger').forEach(btn => btn.addEventListener('click', handlePermissionDelete));
}

// -----------------------------------------------------
function initPagination() {
    document.querySelectorAll('.permission-page-link').forEach(link => {
        link.addEventListener('click', e => {
            e.preventDefault();
            const page = link.dataset.page;
            loadPermissions(page);
        });
    });
}

// --------------------------------------------------
function initSortAndPageSize() {
    const pageSize = document.getElementById('pageSize');
    if (pageSize) pageSize.addEventListener('change', () => loadPermissions(0));
}
// ---------------------------------------------------------
function debounce(fn, delay) {
    let timeoutId;
    return function(...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => fn.apply(this, args), delay);
    };
}

// -----------------------------------------------------
function initSearchInput() {
    const searchPermissionNameInput = document.getElementById('searchPermissionName');
    if (searchPermissionNameInput) {
        const debouncedLoad = debounce(() => loadPermissions(0), 700);
        searchPermissionNameInput.addEventListener('input', () => {
            debouncedLoad();
        });
    }
}


// ----------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
    loadPermissions();

    const form = document.getElementById('permissionForm');
    if (form) form.addEventListener('submit', handlePermissionSubmit);

    const modalTitle = document.getElementById('permissionModalLabel');

    // دکمه افزودن دسترسی
    const addPermissionButton = document.querySelector('[data-bs-target="#permissionModal"]');
    if (addPermissionButton) {
        addPermissionButton.addEventListener('click', () => {
            modalTitle.textContent = modalTitle.dataset.titleAdd; // پیش‌فرض
            document.getElementById('permissionId').value = '';
            document.getElementById('permissionName').value = '';
        });
    }

    // دکمه‌های ویرایش
    initEditButtons();
    initAddButtons();
    initPagination();
    initSortAndPageSize();
    initDeleteButtons();

    //  برگرداندن عنوان به حالت پیش‌فرض بعد از بسته شدن مودال
    document.getElementById('permissionModal').addEventListener('hide.bs.modal', () => {
        modalTitle.textContent = modalTitle.dataset.titleAdd;
        const focusedElement = document.querySelector('#permissionModal :focus');
        if (focusedElement) focusedElement.blur();
    });

    //  گوش دادن به event باز شدن مودال برای اطمینان
    document.getElementById('permissionModal').addEventListener('show.bs.modal', (event) => {
        const modalTitle = document.getElementById('permissionModalLabel');

        // اگر دکمه بازکننده مودال دارای کلاس btn-add بود، یعنی افزودن است
        if (event.relatedTarget && event.relatedTarget.classList.contains('btn-add')) {
            modalTitle.textContent = modalTitle.dataset.titleAdd;
            document.getElementById('permissionId').value = '';
            document.getElementById('permissionName').value = '';
        }
    });

});

