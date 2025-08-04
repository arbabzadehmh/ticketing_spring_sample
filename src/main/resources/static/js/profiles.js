function loadProfiles(page = 0) {
    // اگر table وجود نداشت، از تابع خارج شو
    const container = document.getElementById('profiles-table-container');
    const pageSizeElement = document.getElementById('pageSize');
    const sortByElement = document.getElementById('sortBy');

    if (!container || !pageSizeElement || !sortByElement) {
        console.log('%cProfiles table not found, skipping loadProfiles()', 'color: orange;');
        return;
    }

    const size = parseInt(pageSizeElement.value, 10);
    const sortBy = sortByElement.value;

    const url = `/profiles?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}&sortBy=${encodeURIComponent(sortBy)}&fragment=true`;

    fetch(url)
        .then(response => {
            if (!response.ok) throw new Error('خطا در دریافت داده‌ها');
            return response.text();
        })
        .then(html => {
            container.innerHTML = html;

            // بازگرداندن مقادیر انتخابی dropdown ها بعد از رندر مجدد
            document.getElementById('pageSize').value = size;
            document.getElementById('sortBy').value = sortBy;

            initPagination();
            initSortAndPageSize();
            initDeleteButtons();
            initEditButtons();

            // ثبت دوباره رویداد submit برای فرم edit
            const editForm = document.getElementById('profileEditForm');
            if (editForm) {
                editForm.addEventListener('submit', handleEditProfileSubmit);
            }
        })
        .catch(error => showToast('danger', error.message || 'خطا در دریافت داده‌ها'));
}


// -------------------- Create Profile --------------------
const isAdmin = document.body.dataset.isAdmin === 'true';

async function handleCreateProfileSubmit(e) {
    e.preventDefault();
    clearValidationErrors();

    const selectedRoles = Array.from(document.querySelectorAll('#profileCreateModal .role-checkbox:checked'))
        .map(cb => cb.value);


    const profile = {
        firstName: document.getElementById('createFirstName').value.trim(),
        lastName: document.getElementById('createLastName').value.trim(),
        email: document.getElementById('createEmail').value.trim(),
        phone: document.getElementById('createPhone').value.trim(),
        username: document.getElementById('createUsername').value.trim(),
        password: document.getElementById('createPassword').value.trim()
    };

    let url = '/profiles/register';
    if (isAdmin) {
        url = '/profiles/admin/create-profile';
        profile.roles = selectedRoles;
    }

    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(profile)
        });

        const data = await handleResponse(response, 'create'); // مدیریت پاسخ با متد استاندارد

        // موفقیت
        bootstrap.Modal.getInstance(document.getElementById('profileCreateModal')).hide();
        showToast('success', data.message || 'پروفایل با موفقیت ثبت شد');
        loadProfiles();

    } catch (error) {
        // خطاهای اعتبارسنجی قبلاً در handleResponse نمایش داده شدند
        if (error.message !== 'Validation errors') {
            console.error('Profile creation error:', error);
            showToast('danger', error.message || 'خطا در ثبت پروفایل');
        }
    }
}


// -------------------- Edit Profile --------------------
async function handleEditProfileSubmit(e) {
    e.preventDefault();
    clearValidationErrors();

    const id = document.getElementById('editProfileId').value;
    const passwordInput = document.getElementById('editPassword').value.trim();
    const isAdmin = document.body.dataset.isAdmin === 'true';
    const profile = {
        firstName: document.getElementById('editFirstName').value.trim(),
        lastName: document.getElementById('editLastName').value.trim(),
        email: document.getElementById('editEmail').value.trim(),
        phone: document.getElementById('editPhone').value.trim()
    };

    // فقط اگر پسورد تغییر کرده باشد به سرور بفرست
    if (passwordInput && passwordInput !== '******') {
        profile.password = passwordInput;
    }

    if (isAdmin) {
        profile.roles = Array.from(document.querySelectorAll('#editRolesDropdownMenu .role-checkbox:checked'))
            .map(cb => cb.value);
        profile.accountNonExpired = document.getElementById('editAccountNonExpired').value === 'true';
        profile.accountNonLocked = document.getElementById('editAccountNonLocked').value === 'true';
        profile.credentialsNonExpired = document.getElementById('editCredentialsNonExpired').value === 'true';
        profile.enabled = document.getElementById('editEnabled').value === 'true';
    }

    try {
        const response = await fetch(`/profiles/${id}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(profile)
        });

        const data = await handleResponse(response, 'edit');

        bootstrap.Modal.getInstance(document.getElementById('profileEditModal')).hide();
        showToast('success', data.message || 'پروفایل با موفقیت ویرایش شد');
        loadProfiles();

    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Profile edition error:', error);
            showToast('danger', error.message || 'خطا در ویرایش پروفایل');
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
function loadRolesForCreateModal() {
    fetch('/rest/roles')
        .then(response => {
            if (!response.ok) throw new Error('خطا در دریافت نقش‌ها');
            return response.json();
        })
        .then(roles => {
            const menu = document.getElementById('rolesDropdownMenu'); //  گرفتن المان منو
            menu.innerHTML = ''; //  پاک کردن نقش‌های قبلی

            roles.forEach(roleName => {
                const li = document.createElement('li');
                li.innerHTML = `
                    <div class="form-check">
                        <input class="form-check-input role-checkbox" type="checkbox" value="${roleName}" id="role-${roleName}">
                       <label class="form-check-label" for="role-${roleName}">
                           ${roleName}
                       </label>
                    </div>
               `;
                menu.appendChild(li);
            });
        })
        .catch(err => {
            console.error('Error loading roles:', err);
        });
}


// بارگذاری نقش‌ها هنگام نمایش مودال
const profileCreateModal = document.getElementById('profileCreateModal');
if (profileCreateModal) {
    profileCreateModal.addEventListener('show.bs.modal', () => {
        loadRolesForCreateModal();
    });
}

// --------------------------------------------------------------
function loadRolesForEditModal(selectedRoles = []) {
    fetch('/rest/roles')
        .then(response => response.json())
        .then(data => {
            const menu = document.getElementById('editRolesDropdownMenu');
            menu.innerHTML = '';

            data.forEach(role => {
                const li = document.createElement('div');
                li.className = 'form-check';
                li.innerHTML = `
                    <input class="form-check-input role-checkbox" type="checkbox" value="${role}" id="edit-role-${role}"
                        ${selectedRoles.includes(role) ? 'checked' : ''}>
                    <label class="form-check-label" for="edit-role-${role}">${role}</label>
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

// -------------------- Delete Profile --------------------
async function handleProfileDelete(e) {
    const btn = e.target.closest('.btn-danger');
    const confirmText = btn.dataset.confirmText;
    if (!confirm(confirmText)) return;

    const id = btn.dataset.id;
    try {
        const response = await fetch(`/profiles/${id}`, {
            method: 'DELETE',
        });

        const data = await handleResponse(response, 'delete');

        showToast('success', data.message || 'پروفایل با موفقیت حذف شد');
        loadProfiles();

    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Profile deletion error:', error);
            showToast('danger', error.message || 'خطا در حذف پروفایل');
        }
    }
}

// -------------------- Init Modals --------------------
function initEditButtons() {
    document.querySelectorAll('.btn-edit').forEach(btn => {
        btn.addEventListener('click', () => {
            const row = btn.closest('tr');
            document.getElementById('editProfileId').value = btn.dataset.id;
            document.getElementById('editFirstName').value = row.cells[0].textContent.trim();
            document.getElementById('editLastName').value = row.cells[1].textContent.trim();
            document.getElementById('editEmail').value = row.cells[2].textContent.trim();
            document.getElementById('editPhone').value = row.cells[3].textContent.trim();
            document.getElementById('editUsername').value = row.cells[4].textContent.trim();
            document.getElementById('editPassword').value = '******';

            const isAdmin = document.body.dataset.isAdmin === 'true';
            if (isAdmin) {
                loadRolesForEditModal(btn.dataset.roles ? btn.dataset.roles.split(',') : []);
                document.getElementById('editAccountNonExpired').value = btn.dataset.accountNonExpired;
                document.getElementById('editAccountNonLocked').value = btn.dataset.accountNonLocked;
                document.getElementById('editCredentialsNonExpired').value = btn.dataset.credentialsNonExpired;
                document.getElementById('editEnabled').value = btn.dataset.enabled;
            }

            new bootstrap.Modal(document.getElementById('profileEditModal')).show();
        });
    });
}

// =================== افزودن event delegation برای دکمه کارت ===================
document.body.addEventListener('click', function (e) {
    const btn = e.target.closest('.btn-warning.card-edit');
    if (!btn) return;

    document.getElementById('editProfileId').value = btn.dataset.id || '';
    document.getElementById('editFirstName').value = btn.dataset.firstname || '';
    document.getElementById('editLastName').value = btn.dataset.lastname || '';
    document.getElementById('editEmail').value = btn.dataset.email || '';
    document.getElementById('editPhone').value = btn.dataset.phone || '';
    document.getElementById('editUsername').value = btn.dataset.username || '';
    document.getElementById('editPassword').value = '******';
    new bootstrap.Modal(document.getElementById('profileEditModal')).show();
});

// -------------------------------------------------------
function initPagination() {
    document.querySelectorAll('.page-link').forEach(link => {
        link.addEventListener('click', e => {
            e.preventDefault();
            const page = link.dataset.page;
            loadProfiles(page); // اینجا صفحه را به تابع بده
        });
    });
}

// --------------------------------------------------
function initSortAndPageSize() {
    const pageSize = document.getElementById('pageSize');
    const sortBy = document.getElementById('sortBy');
    if (pageSize) pageSize.addEventListener('change', () => loadProfiles(0));
    if (sortBy) sortBy.addEventListener('change', () => loadProfiles(0));
}


// -------------------------------------------------
function initDeleteButtons() {
    document.querySelectorAll('.btn-danger').forEach(btn => btn.addEventListener('click', handleProfileDelete));
}

// -------------------- DOMContentLoaded --------------------
document.addEventListener('DOMContentLoaded', () => {
    loadProfiles();

    const createForm = document.getElementById('profileCreateForm');
    if (createForm) createForm.addEventListener('submit', handleCreateProfileSubmit);

    const editForm = document.getElementById('profileEditForm');
    if (editForm) editForm.addEventListener('submit', handleEditProfileSubmit);

    initPagination();
    initSortAndPageSize();
    initDeleteButtons();
});
