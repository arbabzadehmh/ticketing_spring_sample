// ******************************************************************** using ProfileApi to findByLastname, findByUsername

let firstLoad = true;

let currentEditingProfileId = null;

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

                // const editForm = document.getElementById('profileEditForm');
                //
                // if (editForm && !editForm.dataset.bound) {
                //     editForm.addEventListener('submit', handleEditProfileSubmit);
                //     editForm.dataset.bound = 'true';
                // }
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

        const imgSrc = p.profilePicture
            ? `/rest/profiles/${p.id}/picture`
            : `/icons/no-image.png`;

        row.innerHTML = `
            <td>${p.firstName}</td>
            <td>${p.lastName}</td>
            <td>${p.email || ''}</td>
            <td>${p.phone || ''}</td>
            <td>${p.user?.username || ''}</td>
            
            <td class="text-center">
                <img src="${imgSrc}"
                     alt="Profile Picture"
                     style="width:50px;height:50px;object-fit:cover;border-radius:50%;border:1px solid #ddd;">
            </td>

            <td>
                <button class="btn btn-sm btn-warning btn-edit"
                    data-id="${p.id}"
                    data-roles='${roles}'
                    data-account-non-expired='${p.user?.accountNonExpired}'
                    data-account-non-locked='${p.user?.accountNonLocked}'
                    data-credentials-non-expired='${p.user?.credentialsNonExpired}'
                    data-enabled='${p.user?.enabled}'
                    data-credentials-expiry-date='${p.user?.credentialsExpiryDate || ''}'
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
        const response = await secureFetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(profile)
        });

        const data = await handleResponse(response, 'create'); // مدیریت پاسخ با متد استاندارد

        //  اضافه‌شده: آپلود عکس پس از ایجاد پروفایل
        const fileInput = document.getElementById('createProfilePicture');
        if (fileInput && fileInput.files.length > 0 && data.id) {
            const file = fileInput.files[0];
            const formData = new FormData();
            formData.append('file', file);

            await secureFetch(`/rest/profiles/${data.id}/picture`, {
                method: 'POST',
                body: formData
            });
        }

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
        phone: document.getElementById('editPhone').value.trim(),
        version: document.getElementById('editProfileVersion').value.trim()
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
        const response = await secureFetch(`/rest/profiles/${id}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(profile)
        });

        const data = await handleResponse(response, 'edit');

        //  اگر کاربر فایل انتخاب کرده، عکس جدید آپلود شود
        const fileInput = document.getElementById('editProfilePicture');
        if (fileInput && fileInput.files.length > 0) {
            const file = fileInput.files[0];
            const formData = new FormData();
            formData.append('file', file);

            const uploadResponse = await secureFetch(`/rest/profiles/${id}/picture`, {
                method: 'POST',
                body: formData
            });

            if (!uploadResponse.ok) {
                throw new Error('خطا در آپلود عکس پروفایل');
            }
        }

        try {
            await secureFetch(`/rest/profiles/${id}/edit-stop`,
                {
                    method: 'POST'
                }
            );
        } catch (e) {
            console.error('profile unlock error', e);
        }

        currentEditingProfileId = null;

        bootstrap.Modal.getInstance(document.getElementById('profileEditModal'))?.hide();
        showToast('success', data.message || 'پروفایل با موفقیت ویرایش شد');


        if (isAdmin) {
            //  حالت ادمین
            loadProfiles();
        } else {
            //  حالت کاستومر: کارت را از سرور تازه بگیر
            refreshProfileCard(id);
        }

    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Profile edition error:', error);
            showToast('danger', error.message || 'خطا در ویرایش پروفایل');
        }
    }
}

// ----------------------------------------------------------------

async function refreshProfileCard(profileId) {
    try {
        const modal = document.getElementById('profileEditModal');

        //  رفع مشکل focus
        document.activeElement?.blur();

        //  بستن مودال (اگر باز است)
        const modalInstance = bootstrap.Modal.getInstance(modal);
        if (modalInstance) {
            modalInstance.hide();
        }

        //  اطمینان از حذف backdrop و فعال‌سازی صفحه بعد از بسته شدن
        setTimeout(async () => {
            // Bootstrap گاهی backdrop را حذف نمی‌کند → دستی حذف می‌کنیم
            document.querySelectorAll('.modal-backdrop').forEach(el => el.remove());
            document.body.classList.remove('modal-open');
            document.body.style.removeProperty('overflow');
            document.body.style.removeProperty('padding-right');

            // حالا درخواست HTML جدید کارت
            const res = await fetch(`/profiles/${profileId}/card`);
            if (!res.ok) throw new Error('خطا در دریافت کارت جدید');

            const html = await res.text();

            // جایگزین کارت جدید
            const card = document.getElementById(`profile-card-${profileId}`);
            if (card) {
                card.outerHTML = html;
            }
        }, 400); // کمی بیشتر تا انیمیشن کامل تمام شود

    } catch (err) {
        console.error('Refresh card error:', err);
        showToast('danger', err.message || 'خطا در به‌روزرسانی کارت پروفایل');
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
if (profileCreateModal && !profileCreateModal.dataset.bound) {
    profileCreateModal.addEventListener(
        'show.bs.modal',
        () => {
            loadRolesForCreateModal();
        }
    );

    profileCreateModal.dataset.bound = 'true';
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
        const response = await secureFetch(`/rest/profiles/${id}`, {
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

// =================== افزودن event delegation برای دکمه کارت ===================
// =================== افزودن event delegation برای دکمه کارت ===================
document.body.addEventListener('click', async function (e) {

    const btn = e.target.closest('.btn-warning.btn-edit');
    if (!btn) return;

    e.preventDefault();
    e.stopPropagation();

    // جلوگیری از کلیک پشت سر هم
    if (btn.dataset.loading === 'true') {
        return;
    }

    btn.dataset.loading = 'true';

    const profileId = btn.dataset.id;

    try {

        const lockResponse = await secureFetch(
            `/rest/profiles/${profileId}/edit-start`,
            {
                method: 'POST'
            }
        );

        let lockData;
        const rawText = await lockResponse.text();

        try {
            lockData = JSON.parse(rawText);
        } catch {
            lockData = {error: rawText};
        }

        if (!lockResponse.ok) {

            showToast(
                'danger',
                lockData.error || 'این پروفایل در حال ویرایش است'
            );

            return;
        }

        currentEditingProfileId = profileId;

        const modal = document.getElementById('profileEditModal');
        const isAdmin = document.body.dataset.isAdmin === 'true';

        const row = btn.closest('tr');

        if (row) {

            // حالت ادمین
            document.getElementById('editProfileId').value = profileId;
            document.getElementById('editFirstName').value = row.cells[0].textContent.trim();
            document.getElementById('editLastName').value = row.cells[1].textContent.trim();
            document.getElementById('editEmail').value = row.cells[2].textContent.trim();
            document.getElementById('editPhone').value = row.cells[3].textContent.trim();
            document.getElementById('editUsername').value = row.cells[4].textContent.trim();

        } else {

            // حالت customer
            document.getElementById('editProfileId').value = profileId;
            document.getElementById('editFirstName').value = btn.dataset.firstname;
            document.getElementById('editLastName').value = btn.dataset.lastname;
            document.getElementById('editEmail').value = btn.dataset.email;
            document.getElementById('editPhone').value = btn.dataset.phone;
            document.getElementById('editUsername').value = btn.dataset.username;
        }

        document.getElementById('editProfileVersion').value =
            btn.dataset.version || '';

        document.getElementById('editPassword').value = '******';

        // ---------------- بخش مربوط به ادمین ----------------
        if (isAdmin && row) {

            const roles = btn.dataset.roles
                ? btn.dataset.roles.split(',')
                : [];

            loadRolesForEditModal(roles);

            document.getElementById('editAccountNonExpired').value =
                btn.dataset.accountNonExpired || '';

            document.getElementById('editAccountNonLocked').value =
                btn.dataset.accountNonLocked || '';

            document.getElementById('editCredentialsNonExpired').value =
                btn.dataset.credentialsNonExpired || '';

            document.getElementById('editEnabled').value =
                btn.dataset.enabled || '';

            const expiryDate = btn.dataset.credentialsExpiryDate;

            const expiryInput =
                document.getElementById('editCredentialsExpiryDate');

            if (expiryDate && expiryInput) {

                if (expiryDate.includes(',')) {

                    const parts =
                        expiryDate.split(',').map(Number);

                    const jsDate = new Date(
                        parts[0],
                        parts[1] - 1,
                        parts[2],
                        parts[3],
                        parts[4],
                        parts[5] || 0
                    );

                    if (!isNaN(jsDate)) {

                        const yyyy = jsDate.getFullYear();
                        const mm = String(jsDate.getMonth() + 1).padStart(2, '0');
                        const dd = String(jsDate.getDate()).padStart(2, '0');
                        const hh = String(jsDate.getHours()).padStart(2, '0');
                        const min = String(jsDate.getMinutes()).padStart(2, '0');

                        expiryInput.value =
                            `${yyyy}-${mm}-${dd}T${hh}:${min}`;

                    } else {

                        expiryInput.value = '';
                    }

                } else {

                    expiryInput.value =
                        expiryDate.substring(0, 16);
                }

            } else if (expiryInput) {

                expiryInput.value = '';
            }
        }

        bootstrap.Modal
            .getOrCreateInstance(modal)
            .show();

    } catch (error) {

        console.error('profile edit-start error:', error);

        showToast(
            'danger',
            'خطا در بررسی قفل ویرایش'
        );

    } finally {

        btn.dataset.loading = 'false';
    }
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

    if (createForm && !createForm.dataset.bound) {
        createForm.addEventListener('submit', handleCreateProfileSubmit);
        createForm.dataset.bound = 'true';
    }

    const editForm = document.getElementById('profileEditForm');

    if (editForm && !editForm.dataset.bound) {
        editForm.addEventListener('submit', handleEditProfileSubmit);
        editForm.dataset.bound = 'true';
    }

    const imgPreview = document.getElementById('editProfileImagePreview');
    const fileInput = document.getElementById('editProfilePicture');
    const deleteBtn = document.getElementById('deleteProfilePicBtn');

    const DEFAULT_IMG = '/icons/no-image.png'; // مسیر عکس پیش‌فرض (قرار بده در static/images)
    let currentObjectUrl = null;

    //  وقتی کاربر فایل جدید انتخاب می‌کند
    if (fileInput) {
        fileInput.addEventListener('change', e => {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = ev => {
                    if (currentObjectUrl) {
                        URL.revokeObjectURL(currentObjectUrl);
                        currentObjectUrl = null;
                    } // 💠 آزادسازی آدرس قبلی
                    imgPreview.src = ev.target.result;
                    imgPreview.style.display = 'block';
                };
                reader.readAsDataURL(file);
                deleteBtn.disabled = false;
            } else {
                imgPreview.src = DEFAULT_IMG;
                deleteBtn.disabled = true;
            }
        });
    }

    //  دکمه حذف عکس پروفایل
    if (deleteBtn) {
        deleteBtn.addEventListener('click', async () => {
            const profileId = document.getElementById('editProfileId').value;
            if (!profileId) return;

            try {
                const response = await secureFetch(`/rest/profiles/${profileId}/picture`, {method: 'DELETE'});
                const data = await handleResponse(response, 'delete');

                if (data.version !== undefined) {
                    document.getElementById('editProfileVersion').value =
                        data.version;
                }

                if (currentObjectUrl) {
                    URL.revokeObjectURL(currentObjectUrl);
                    currentObjectUrl = null;
                } //  آزادسازی
                imgPreview.src = DEFAULT_IMG;  // نمایش "no image"
                fileInput.value = '';          // خالی کردن input فایل
                deleteBtn.disabled = true;

                showToast('success', data.message || 'عکس حذف شد');
            } catch (error) {
                console.error('Delete picture error:', error);
                showToast('danger', error.message || 'خطا در حذف عکس');
            }
        });
    }

    const editModal = document.getElementById('profileEditModal');
    if (editModal) {
        editModal.addEventListener('show.bs.modal', () => {
            const profileId = document.getElementById('editProfileId').value;
            if (!profileId) {
                imgPreview.src = DEFAULT_IMG;
                deleteBtn.disabled = true;
                return;
            }

            fetch(`/rest/profiles/${profileId}/picture`)
                .then(res => (res.ok ? res.blob() : null))
                .then(blob => {
                    if (currentObjectUrl) {
                        URL.revokeObjectURL(currentObjectUrl);
                        currentObjectUrl = null;
                    } //  پاک‌سازی قبلی
                    if (blob) {
                        currentObjectUrl = URL.createObjectURL(blob);
                        imgPreview.src = currentObjectUrl;
                        imgPreview.style.display = 'block';
                        deleteBtn.disabled = false;
                    } else {
                        imgPreview.src = DEFAULT_IMG;
                        deleteBtn.disabled = true;
                    }
                    fileInput.value = ''; //  حتما ورودی فایل پاک شود
                })
                .catch(() => {
                    imgPreview.src = DEFAULT_IMG;
                    deleteBtn.disabled = true;
                    fileInput.value = '';
                });
        });

        //  هنگام بستن مودال (آزادسازی object URL)
        // هنگام بستن مودال
        editModal.addEventListener(
            'hidden.bs.modal',
            async () => {

                // آزادسازی preview image
                if (currentObjectUrl) {
                    URL.revokeObjectURL(currentObjectUrl);
                    currentObjectUrl = null;
                }

                const profileId = currentEditingProfileId;

                currentEditingProfileId = null;

                if (!profileId) return;

                try {

                    await secureFetch(
                        `/rest/profiles/${profileId}/edit-stop`,
                        {
                            method: 'POST'
                        }
                    );

                } catch (e) {

                    console.error(
                        'profile edit-stop error',
                        e
                    );
                }

                document.getElementById('profileEditForm')?.reset();

                clearValidationErrors();
            }
        );
    }

    const createFileInput = document.getElementById('createProfilePicture');
    const createImgPreview = document.getElementById('createProfileImagePreview');
    const deleteCreateBtn = document.getElementById('deleteCreatePicBtn');

    if (createFileInput && createImgPreview) {
        createFileInput.addEventListener('change', e => {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = ev => {
                    createImgPreview.src = ev.target.result;
                    createImgPreview.style.display = 'block';
                };
                reader.readAsDataURL(file);
                deleteCreateBtn.disabled = false;
            } else {
                createImgPreview.src = DEFAULT_IMG;
                deleteCreateBtn.disabled = true;
            }
        });
    }

    if (deleteCreateBtn) {
        deleteCreateBtn.addEventListener('click', () => {
            createImgPreview.src = DEFAULT_IMG;
            createFileInput.value = '';
            deleteCreateBtn.disabled = true;
        });
    }

});
