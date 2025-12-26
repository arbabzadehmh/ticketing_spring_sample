let firstLoad = true;

let phones = [];

function escapeHtml(text) {
    if (text == null) return '';
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}


// -------------------- Load Buildings --------------------
function loadBuildings(page = 0) {
    const container = document.getElementById('buildings-table-container');
    const pageSizeElement = document.getElementById('pageSize');
    const searchBuildingTitleElement = document.getElementById('searchBuildingTitle');

    if (!container || !pageSizeElement) {
        console.log('%cBuildings table not found, skipping loadBuildings()', 'color: orange;');
        return;
    }

    const size = parseInt(pageSizeElement.value, 10);
    const searchBuildingTitle = searchBuildingTitleElement ? searchBuildingTitleElement.value.trim() : '';

    // --- بار اول: از WebController (fragment) ---
    if (firstLoad) {
        firstLoad = false;

        console.log("web controller / fetch")

        const url = `/buildings?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}&fragment=true`;
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

                const newSearchBuildingTitleElement = document.getElementById('searchBuildingTitle');
                if (newSearchBuildingTitleElement) newSearchBuildingTitleElement.value = searchBuildingTitle;

                // REMOVED: هیچ initXYZ اینجا صدا زده نمی‌شه. (delegation داریم)
                // اگر فرم‌های داخل مودال‌ها وجود دارن، submitشون رو یکبار bind می‌کنیم
                const editForm = document.getElementById('buildingEditForm');
                if (editForm && !editForm.dataset.bound) {
                    editForm.addEventListener('submit', handleEditBuildingSubmit);
                    editForm.dataset.bound = 'true'; // CHANGED: جلوگیری از دوباره بایند شدن
                }

            })
            .catch(err => showToast('danger', err.message || 'خطا در دریافت داده‌ها'));
        return;
    }

    // --- دفعات بعد: RestController (JSON) ---
    console.log("rest controller / fetch")

    let apiUrl = `/rest/buildings/get-all?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}`;
    if (searchBuildingTitle) apiUrl += `&searchBuildingTitle=${encodeURIComponent(searchBuildingTitle)}`;

    // ذخیره صفحه جاری
    const currentPage = page;

    fetch(apiUrl)
        .then(res => {
            if (!res.ok) throw new Error('خطا در دریافت داده‌ها');
            return res.json();
        })
        .then(data => {
            renderBuildingsTable(data.content);
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
function renderBuildingsTable(buildings) {
    const tableBody = document.querySelector("#buildings-table tbody");
    if (!tableBody) return;

    tableBody.innerHTML = '';

    buildings.forEach(b => {
        const row = document.createElement('tr');

        const sections = Array.isArray(b.sections)
            ? b.sections.join(', ')
            : '';

        const phones = Array.isArray(b.phones)
            ? b.phones.join(', ')
            : '';

        row.innerHTML = `
      <td>${b.title}</td>
      <td>${escapeHtml(sections)}</td>
      <td>${escapeHtml(phones)}</td>
      <td>${escapeHtml(b.fullAddress ?? '-')}</td>
      <td>
        <button class="btn btn-sm btn-warning btn-edit"
          data-id="${b.id}">
          <i class="fas fa-edit"></i>
        </button>
        <button class="btn btn-sm btn-danger btn-delete"
          data-id="${b.id}">
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

// -------------------- Create Building --------------------
async function handleCreateBuildingSubmit(e) {
    e.preventDefault();
    clearValidationErrors();

    // --- Address ---
    const addressSelect = document.getElementById('addressSelect');
    const addressId = addressSelect ? addressSelect.value : null;

    let addressDto = null;
    if (!addressId && !document.getElementById('newAddressContainer').classList.contains('d-none')) {
        addressDto = {
            country: addrCountry.value,
            state: addrState.value,
            city: addrCity.value,
            village: addrVillage.value,
            region: addrRegion.value,
            street: addrStreet.value,
            platesNumber: addrPlate.value,
            floor: addrFloor.value,
            unit: addrUnit.value,
            postalCode: addrPostal.value
        };
    }

    console.log(addressDto);

    // --- Building ---
    const building = {
        title: document.getElementById('createTitle').value.trim(),
        phoneNumbers: phones,
        sectionList: Array.from(
            document.querySelectorAll('#sectionsContainer .section-checkbox:checked')
        ).map(cb => ({id: cb.value})),
        addressId: addressId || null
    };

    console.log(building);

    const payload = {
        building,
        addressDto
    };

    try {
        const response = await fetch('/rest/buildings', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        });

        const data = await handleResponse(response, 'create');

        bootstrap.Modal
            .getInstance(document.getElementById('buildingCreateModal'))
            ?.hide();

        showToast('success', data.message || 'بیلدینگ با موفقیت ثبت شد');

        phones = [];
        renderPhones();

        loadBuildings();

    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Building creation error:', error);
            showToast('danger', error.message || 'خطا در ثبت بیلدینگ');
        }
    }
}

// ---------------------------------------------------------
function renderPhones() {
    const phonesList = document.getElementById('phonesList');
    if (!phonesList) return;

    phonesList.innerHTML = '';

    phones.forEach((phone, index) => {
        const badge = document.createElement('span');
        badge.className = 'badge bg-primary d-flex align-items-center gap-1';
        badge.innerHTML = `
            ${phone}
            <button type="button"
                    class="btn btn-sm btn-close btn-close-white"
                    data-index="${index}">
            </button>
        `;
        phonesList.appendChild(badge);
    });
}

// -------------------- Load sections into create modal --------------------
function loadSectionsForCreateModal() {
    fetch('/rest/sections/get-all')
        .then(response => {
            if (!response.ok) throw new Error('خطا در دریافت سکشن‌ها');
            return response.json();
        })
        .then(data => {
            const sectionsArray = Array.isArray(data) ? data : data.content || [];
            const container = document.getElementById('sectionsContainer');
            if (!container) return;

            container.innerHTML = '';

            sectionsArray.forEach(section => {
                const div = document.createElement('div');
                div.className = 'form-check';

                div.innerHTML = `
                    <input class="form-check-input section-checkbox"
                           type="checkbox"
                           value="${section.id}"
                           id="section-${section.id}">
                    <label class="form-check-label" for="section-${section.id}">
                        ${section.title}
                    </label>
                `;

                container.appendChild(div);
            });
        })
        .catch(err => console.error('Error loading sections:', err));
}


const buildingCreateModal = document.getElementById('buildingCreateModal');

if (buildingCreateModal && !buildingCreateModal.dataset.bound) {

    buildingCreateModal.addEventListener('shown.bs.modal', () => {
        loadSectionsForCreateModal();
        loadAddressesForCreateModal();
    });

    buildingCreateModal.dataset.bound = 'true';
}

// -------------------------------------------------
// function buildAddressDtoIfNew() {
//     const container = document.getElementById('newAddressContainer');
//
//     if (container.classList.contains('d-none')) {
//         return null;
//     }
//
//     return {
//         country: addrCountry.value?.trim(),
//         state: addrState.value?.trim(),
//         city: addrCity.value?.trim(),
//         village: addrVillage.value?.trim(),
//         region: addrRegion.value?.trim(),
//         street: addrStreet.value?.trim(),
//         platesNumber: addrPlate.value?.trim(),
//         floor: addrFloor.value?.trim(),
//         unit: addrUnit.value?.trim(),
//         postalCode: addrPostal.value?.trim()
//     };
// }

// ----------------------------------------------------------
function loadAddressesForCreateModal() {
    fetch('/rest/addresses')
        .then(response => {
            if (!response.ok) throw new Error('خطا در دریافت آدرس‌ها');
            return response.json();
        })
        .then(response => {

            // 👇 این خط مهم است
            const addresses = response.body ?? response;

            const select = document.getElementById('addressSelect');
            if (!select) return;

            select.innerHTML = `<option value="">انتخاب آدرس</option>`;

            addresses.forEach(addr => {
                const option = document.createElement('option');
                option.value = addr.id;
                option.textContent = formatAddress(addr);
                select.appendChild(option);
            });
        })
        .catch(err => console.error('Error loading addresses:', err));
}


function formatAddress(addr) {
    return [
        addr.country,
        addr.state,
        addr.city,
        addr.village,
        addr.region,
        addr.street,
        addr.platesNumber,
        addr.floor,
        addr.unit,
        addr.postalCode
    ].filter(Boolean).join(' - ');
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

// -------------------- Validation helpers --------------------
function displayValidationErrors(errors, mode) {
    clearValidationErrors();

    for (const field in errors) {
        const input = document.querySelector(`[name="${field}"]`);

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

// -------------------- Delete Building --------------------
async function handleBuildingDelete(e) {
    const btn = e.target.closest('.btn-delete, .btn-danger');
    if (!btn) return;
    const confirmTextElem = document.getElementById('deleteConfirmText');
    const confirmText = confirmTextElem ? confirmTextElem.textContent.trim() : "Are you sure?";
    if (!confirm(confirmText)) return;

    const id = btn.dataset.id;
    try {
        const response = await fetch(`/rest/buildings/${id}`, {method: 'DELETE'});
        const data = await handleResponse(response, 'delete');
        showToast('success', data.message || 'ساختمان با موفقیت حذف شد');
        loadBuildings();
    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Building deletion error:', error);
            showToast('danger', error.message || 'خطا در حذف ساختمان');
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
    loadBuildings();

    const addPhoneBtn = document.getElementById('addPhoneBtn');
    const phoneInput = document.getElementById('phoneInput');

    addPhoneBtn?.addEventListener('click', () => {
        const value = phoneInput.value.trim();
        if (!value) return;

        if (phones.includes(value)) {
            showToast('warning', 'این شماره قبلاً اضافه شده');
            return;
        }

        phones.push(value);
        renderPhones();
        phoneInput.value = '';
    });

    const addNewAddressBtn = document.getElementById('addNewAddressBtn');
    addNewAddressBtn?.addEventListener('click', () => {
        document.getElementById('newAddressContainer')?.classList.toggle('d-none');
    });


    // فرم‌ها
    const createForm = document.getElementById('buildingCreateForm');
    if (createForm && !createForm.dataset.bound) {
        createForm.addEventListener('submit', handleCreateBuildingSubmit);
        createForm.dataset.bound = 'true';
    }


    const editForm = document.getElementById('buildingEditForm');
    if (editForm && !editForm.dataset.bound) {
        editForm.addEventListener('submit', handleEditBuildingSubmit);
        editForm.dataset.bound = 'true';
    }

    // کلیک‌ها (delegation)
    document.body.addEventListener('click', e => {
        const btnDelete = e.target.closest('.btn-delete, .btn-danger');
        if (btnDelete) {
            handleBuildingDelete(e);
            return;
        }

        //  ADDED: حذف تلفن
        const removePhoneBtn = e.target.closest('.btn-close');
        if (removePhoneBtn && removePhoneBtn.dataset.index !== undefined) {
            phones.splice(removePhoneBtn.dataset.index, 1);
            renderPhones();
            return;
        }

        // todo:
        const btnEdit = e.target.closest('.btn-edit');
        if (btnEdit) {
            const row = btnEdit.closest('tr');
            document.getElementById('editRoleName').value = btnEdit.dataset.id || '';
            document.getElementById('editName').value = row ? row.cells[0].textContent.trim() : '';
            const selected = btnEdit.dataset.permissions ? btnEdit.dataset.permissions.split(',') : [];
            loadPermissionsForEditModal(selected);
            new bootstrap.Modal(document.getElementById('roleEditModal')).show();
            return;
        }

        const pageLink = e.target.closest('.page-link');
        if (pageLink) {
            e.preventDefault();
            const pageAttr = pageLink.dataset.page;
            const page = pageAttr != null ? parseInt(pageAttr, 10) : (parseInt(pageLink.textContent, 10) - 1);
            if (!isNaN(page)) loadBuildings(page);
            return;
        }
    });

    const debouncedLoad = debounce(() => loadBuildings(0), 700);
    document.body.addEventListener('input', e => {
        if (e.target.id === 'searchBuildingTitle') debouncedLoad();
    });
    document.body.addEventListener('change', e => {
        if (e.target.id === 'pageSize') loadBuildings(0);
    });

});