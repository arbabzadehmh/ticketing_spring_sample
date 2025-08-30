
// ******************************************************************** using ProfileApi to findByLastname, findByUsername

let firstLoad = true;

function loadProfiles(page = 0) {
    const container = document.getElementById('profiles-table-container');
    const pageSizeElement = document.getElementById('pageSize');
    const sortByElement = document.getElementById('sortBy');

    const lastNameInput = document.getElementById('searchLastName');
    const usernameInput = document.getElementById('searchUsername');

    if (!container || !pageSizeElement || !sortByElement) {
        console.log('%cProfiles table not found, skipping loadProfiles()', 'color: orange;');
        return;
    }

    const size = parseInt(pageSizeElement.value, 10);
    const sortBy = sortByElement.value;

    const lastName = lastNameInput ? lastNameInput.value.trim() : '';
    const username = usernameInput ? usernameInput.value.trim() : '';

    // ---------- بار اول: WebController ----------
    if (firstLoad) {
        firstLoad = false;

        console.log("web controller / fetch")

        let url = `/profiles?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}&sortBy=${encodeURIComponent(sortBy)}&fragment=true`;

        fetch(url)
            .then(response => {
                if (!response.ok) throw new Error('خطا در دریافت داده‌ها');
                return response.text();
            })
            .then(html => {
                container.innerHTML = html;

                document.getElementById('pageSize').value = size;
                document.getElementById('sortBy').value = sortBy;

                const editForm = document.getElementById('profileEditForm');
                if (editForm) {
                    editForm.addEventListener('submit', handleEditProfileSubmit);
                }
            })
            .catch(error => showToast('danger', error.message || 'خطا در دریافت داده‌ها'));

        return; // خیلی مهم: بعد از بار اول اینجا return کن
    }

    console.log("rest controller / fetch")


    // ---------- دفعات بعد: RestController ----------
    let apiUrl = `/rest/profiles?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}&sortBy=${encodeURIComponent(sortBy)}`;

    if (lastName) {
        apiUrl += `&lastName=${encodeURIComponent(lastName)}`;
    } else if (username) {
        apiUrl += `&username=${encodeURIComponent(username)}`;
    }

    // ذخیره صفحه جاری
    const currentPage = page;

    fetch(apiUrl)
        .then(response => {
            if (!response.ok) throw new Error('خطا در دریافت داده‌ها');
            return response.json();
        })
        .then(data => {
            renderProfilesTable(data.content);
            renderPagination(data, currentPage);

            // نگه داشتن انتخاب‌ها
            document.getElementById('pageSize').value = size;
            document.getElementById('sortBy').value = sortBy;

            // اضافه: بعد از رندر pagination، دوباره روی همان صفحه بایست
            const paginationContainer = document.querySelector(".pagination");
            const activeLink = paginationContainer.querySelector(`.page-link[data-page='${currentPage}']`);
            if (activeLink) activeLink.parentElement.classList.add('active');
        })
        .catch(error => showToast('danger', error.message || 'خطا در دریافت داده‌ها'));
}

function renderProfilesTable(profiles) {
    const tableBody = document.querySelector("#profiles-table tbody");
    tableBody.innerHTML = '';

    profiles.forEach(p => {
        const row = document.createElement('tr');

        const roles = p.user?.roleSet
            ? p.user.roleSet.map(r => r.name).join(",")
            : '';

        row.innerHTML = `
            <td>${p.firstName}</td>
            <td>${p.lastName}</td>
            <td>${p.email || ''}</td>
            <td>${p.phone || ''}</td>
            <td>${p.user?.username || ''}</td>
            <td>
                <button class="btn btn-sm btn-warning btn-edit"
                    data-id="${p.id}"
                    data-roles='${roles}'
                    data-account-non-expired='${p.user?.accountNonExpired}'
                    data-account-non-locked='${p.user?.accountNonLocked}'
                    data-credentials-non-expired='${p.user?.credentialsNonExpired}'
                    data-enabled='${p.user?.enabled}'
                    data-credentials-expiry-date='${p.user?.credentialsExpiryDate || ''}'>
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

// ***************************************************************************************

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

    let url = '/rest/profiles/register';
    if (isAdmin) {
        url = '/rest/profiles/create-profile';
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

        const expiryVal = document.getElementById('editCredentialsExpiryDate').value;
        profile.credentialsExpiryDate = expiryVal ? expiryVal + ':00' : null;
    }

    try {
        const response = await fetch(`/rest/profiles/${id}`, {
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
    const confirmTextElem = document.getElementById('deleteConfirmText');
    const confirmText = confirmTextElem ? confirmTextElem.textContent.trim() : "Are you sure?";
    if (!confirm(confirmText)) return;

    const id = btn.dataset.id;
    try {
        const response = await fetch(`/rest/profiles/${id}`, {
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

// =================== افزودن event delegation برای دکمه کارت ===================
document.body.addEventListener('click', function (e) {
    const btn = e.target.closest('.btn-warning.btn-edit');
    if (!btn) return;

    const row = btn.closest('tr'); // پیدا کردن ردیف جدول مربوطه
    const cells = row.querySelectorAll('td'); // گرفتن همه سلول‌ها

    // فیلدهای اصلی
    document.getElementById('editProfileId').value = btn.dataset.id;
    document.getElementById('editFirstName').value = row.cells[0].textContent.trim();
    document.getElementById('editLastName').value = row.cells[1].textContent.trim();
    document.getElementById('editEmail').value = row.cells[2].textContent.trim();
    document.getElementById('editPhone').value = row.cells[3].textContent.trim();
    document.getElementById('editUsername').value = row.cells[4].textContent.trim();
    document.getElementById('editPassword').value = '******';

    // ---------------- بخش مربوط به ادمین ----------------
    const isAdmin = document.body.dataset.isAdmin === 'true';
    if (isAdmin) {
        const roles = btn.dataset.roles ? btn.dataset.roles.split(',') : [];
        loadRolesForEditModal(roles);

        document.getElementById('editAccountNonExpired').value = btn.dataset.accountNonExpired || '';
        document.getElementById('editAccountNonLocked').value = btn.dataset.accountNonLocked || '';
        document.getElementById('editCredentialsNonExpired').value = btn.dataset.credentialsNonExpired || '';
        document.getElementById('editEnabled').value = btn.dataset.enabled || '';

        const expiryDate = btn.dataset.credentialsExpiryDate;
        const expiryInput = document.getElementById('editCredentialsExpiryDate');

        if (expiryDate && expiryInput) {
            if (expiryDate.includes(',')) {
                const parts = expiryDate.split(',').map(Number); // [2026,2,15,12,38,40]
                const jsDate = new Date(parts[0], parts[1] - 1, parts[2], parts[3], parts[4], parts[5] || 0);

                if (!isNaN(jsDate)) {
                    const yyyy = jsDate.getFullYear();
                    const mm = String(jsDate.getMonth() + 1).padStart(2, '0');
                    const dd = String(jsDate.getDate()).padStart(2, '0');
                    const hh = String(jsDate.getHours()).padStart(2, '0');
                    const min = String(jsDate.getMinutes()).padStart(2, '0');
                    expiryInput.value = `${yyyy}-${mm}-${dd}T${hh}:${min}`;
                } else {
                    expiryInput.value = '';
                }
            } else {
                expiryInput.value = expiryDate.substring(0, 16);
            }
        } else if (expiryInput) {
            expiryInput.value = '';
        }
    }
    // -----------------------------------------------------

    new bootstrap.Modal(document.getElementById('profileEditModal')).show();
});

// ---------------- حذف پروفایل ----------------
document.body.addEventListener('click', function (e) {
    const btn = e.target.closest('.btn-danger');
    if (!btn) return;
    handleProfileDelete(e);
});

// ---------------- صفحه بندی ----------------
document.body.addEventListener('click', e => {
    const pageLink = e.target.closest('.page-link');
    if (pageLink) {
        e.preventDefault();
        const page = parseInt(pageLink.dataset.page, 10);
        if (!isNaN(page)) loadProfiles(page);
        return;
    }
});


// ---------------- sort & page size ----------------
document.body.addEventListener('change', function (e) {
    if (e.target.id === 'pageSize' || e.target.id === 'sortBy') {
        loadProfiles(0);
    }
});

// ---------------- search inputs ----------------
const debouncedLoad = debounce(() => loadProfiles(0), 700);
document.body.addEventListener('input', function (e) {
    const lastnameInput = document.getElementById('searchLastName');
    const usernameInput = document.getElementById('searchUsername');
    if (!lastnameInput || !usernameInput) return;
    if (e.target.id === 'searchLastName') {
        usernameInput.value = '';
        debouncedLoad();
    }
    if (e.target.id === 'searchUsername') {
        lastnameInput.value = '';
        debouncedLoad();
    }
});
// ------------------------------------------------------
function debounce(fn, delay) {
    let timeoutId;
    return function (...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => fn.apply(this, args), delay);
    };
}

// -------------------- DOMContentLoaded --------------------
document.addEventListener('DOMContentLoaded', () => {
    loadProfiles();

    const createForm = document.getElementById('profileCreateForm');
    if (createForm) createForm.addEventListener('submit', handleCreateProfileSubmit);

    const editForm = document.getElementById('profileEditForm');
    if (editForm) editForm.addEventListener('submit', handleEditProfileSubmit);

});
