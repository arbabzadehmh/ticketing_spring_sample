let firstLoad = true;

function loadSections(page = 0) {

    const container = document.getElementById('sections-table-container');
    const pageSizeElement = document.getElementById('pageSize');


// فرض می‌کنیم dataset.roles این رشته رو داده:
    let rawRoles = document.body.dataset.roles || '';

// حذف براکت‌ها و فاصله‌های اضافی
    const roles = rawRoles ? rawRoles.replace(/^\[|\]$/g, '').split(',').map(r => r.trim()) : [];


    const sectionTitleInput = document.getElementById('searchSectionTitle');
    const parentSectionTitleInput = document.getElementById('searchParentSectionTitle');

    if (!container || !pageSizeElement) {
        console.log('%cSections table not found, skipping loadSections()', 'color: orange;');
        return;
    }

    const size = parseInt(pageSizeElement.value, 10);

    const sectionTitle = sectionTitleInput ? sectionTitleInput.value.trim() : '';
    const parentSectionTitle = parentSectionTitleInput ? parentSectionTitleInput.value.trim() : '';

    if (firstLoad) {
        firstLoad = false;

        console.log("web controller / fetch")

        let url = `/sections?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}&fragment=true`;


        fetch(url)
            .then(response => {
                if (!response.ok) throw new Error('خطا در دریافت داده‌ها');
                return response.text();
            })
            .then(html => {
                container.innerHTML = html;

                document.getElementById('pageSize').value = size;

            })
            .catch(error => showToast('danger', error.message || 'خطا در دریافت داده‌ها'));

        return;
    }

    console.log("rest controller / fetch")
    let apiUrl = `/rest/sections/get-all?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}`;

    if (sectionTitle) {
        apiUrl += `&sectionTitle=${encodeURIComponent(sectionTitle)}`;
    } else if (parentSectionTitle) {
        apiUrl += `&parentSectionTitle=${encodeURIComponent(parentSectionTitle)}`;
    }

    const currentPage = page;

    fetch(apiUrl)
        .then(response => {
            if (!response.ok) throw new Error('خطا در دریافت داده‌ها');
            return response.json();
        })
        .then(data => {
            renderSectionsTable(data.content, roles);
            renderPagination(data, currentPage);

            document.getElementById('pageSize').value = size;

        })
        .catch(error => showToast('danger', error.message || 'خطا در دریافت داده‌ها'));
}

// -------------------- Render Table --------------------
// function renderSectionsTable(sections) {
//     const tableBody = document.querySelector("#sections-table tbody");
//     tableBody.innerHTML = '';
//
//     sections.forEach(s => {
//         const row = document.createElement('tr');
//
//         row.innerHTML = `
//             <td>${s.title}</td>
//             <td>${s.parentSection ? s.parentSection.title : '-'}</td>
//             <td>
//                 <button class="btn btn-sm btn-warning btn-edit"
//                     data-id="${s.id}"
//                     data-title="${s.title}"
//                     data-parent="${s.parentSection ? s.parentSection.id : ''}">
//                     <i class="fas fa-edit"></i>
//                 </button>
//                 <button class="btn btn-sm btn-danger btn-delete" data-id="${s.id}">
//                     <i class="fas fa-trash"></i>
//                 </button>
//             </td>
//         `;
//
//         tableBody.appendChild(row);
//     });
// }

function renderSectionsTable(sections, roles) {
    const tableBody = document.querySelector("#sections-table tbody");
    tableBody.innerHTML = '';

    sections.forEach(s => {
        const canEdit = roles.includes("ROLE_ADMIN") || roles.includes("ROLE_MANAGER");

        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${s.title}</td>
            <td>${s.parentSection ? s.parentSection.title : '—'}</td>
            <td>
                ${canEdit ? `
                    <button class="btn btn-sm btn-warning btn-edit"
                        data-id="${s.id}"
                        data-title="${s.title}"
                        data-parent="${s.parentSection ? s.parentSection.id : ''}">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-danger btn-delete" data-id="${s.id}">
                        <i class="fas fa-trash"></i>
                    </button>
                ` : ''}
            </td>
        `;

        tableBody.appendChild(row);
    });
}


// -------------------- Render Pagination --------------------
function renderPagination(data, currentPage = 0) {
    const paginationContainer = document.querySelector(".pagination");
    if (!paginationContainer) return;

    paginationContainer.innerHTML = '';

    for (let i = 0; i < data.totalPages; i++) {
        const li = document.createElement('li');
        li.className = 'page-item' + (i === currentPage ? ' active' : '');
        const a = document.createElement('a');
        a.className = 'page-link';
        a.textContent = (i + 1);
        a.href = '#';
        a.dataset.page = i;
        li.appendChild(a);
        paginationContainer.appendChild(li);
    }
}

// ----------------------------------------------------
async function handleSectionSubmit(e) {
    e.preventDefault();
    clearValidationErrors();

    const id = document.getElementById('sectionId').value;
    const parentSectionId = document.getElementById('parentSection').value.trim();
    const section = {
        title: document.getElementById('title').value.trim(),
        parentSection: parentSectionId ? { id: parentSectionId } : null
    };

    const url = id ? `/rest/sections/${id}` : '/rest/sections';
    const method = id ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, {
            method,
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(section)
        });

        const data = await handleResponse(response, id ? 'edit' : 'create');

        bootstrap.Modal.getInstance(document.getElementById('sectionModal')).hide();
        showToast('success', data.message || (id ? 'بخش ویرایش شد' : 'بخش ثبت شد'));
        loadSections();
        await updateParentSectionOptions();
    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Section save error:', error);
            showToast('danger', error.message || 'خطا در ذخیره بخش');
        }
    }
}

// ----------------------------------------------------------------

async function updateParentSectionOptions() {
    try {
        const response = await fetch('/rest/sections');
        const sections = await response.json();
        const select = document.getElementById('parentSection');
        if (!select) return;

        select.innerHTML = '<option value="">—</option>';
        sections.forEach(s => {
            const option = document.createElement('option');
            option.value = s.id;
            option.textContent = s.title;
            select.appendChild(option);
        });
    } catch (err) {
        console.error('Error updating parent section options:', err);
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

function clearValidationErrors() {
    document.querySelectorAll('.is-invalid').forEach(i => i.classList.remove('is-invalid'));
    document.querySelectorAll('.invalid-feedback').forEach(f => f.remove());
}

function capitalize(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
}

// -------------------------------------------------------
async function handleSectionDelete(e) {
    const btn = e.target.closest('.btn-danger');
    const confirmTextElem = document.getElementById('deleteConfirmText');
    const confirmText = confirmTextElem ? confirmTextElem.textContent.trim() : "Are you sure?";
    if (!confirm(confirmText)) return;

    const id = btn.dataset.id;

    try {
        const response = await fetch(`/rest/sections/${id}`, {method: 'DELETE'});
        const data = await handleResponse(response, 'delete');
        showToast('success', data.message || 'بخش حذف شد');
        loadSections();
        await updateParentSectionOptions();
    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Section deletion error:', error);
            showToast('danger', error.message || 'خطا در حذف بخش');
        }
    }
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
    loadSections();

    const form = document.getElementById('sectionForm');
    if (form) {
        form.addEventListener('submit', handleSectionSubmit);
    }

    const modalTitle = document.getElementById('sectionModalLabel');
    const sectionModal = document.getElementById('sectionModal');

    if (sectionModal) {
        sectionModal.addEventListener('show.bs.modal', (event) => {
            const isAdd = event.relatedTarget && event.relatedTarget.classList.contains('btn-add');
            if (isAdd) {
                modalTitle.textContent = modalTitle.dataset.titleAdd;
                document.getElementById('sectionId').value = '';
                document.getElementById('title').value = '';
                if (document.getElementById('parentSection')) {
                    document.getElementById('parentSection').value = '';
                }
            }
        });

        sectionModal.addEventListener('hide.bs.modal', () => {
            modalTitle.textContent = modalTitle.dataset.titleAdd;
            const focusedElement = sectionModal.querySelector(':focus');
            if (focusedElement) focusedElement.blur();
        });
    }

    document.body.addEventListener('click', (e) => {
        const btnDelete = e.target.closest('.btn-delete, .btn-danger');
        if (btnDelete) {
            handleSectionDelete(e);
            return;
        }

        const btnEdit = e.target.closest('.btn-edit');
        if (btnEdit) {
            modalTitle.textContent = modalTitle.dataset.titleEdit;
            document.getElementById('sectionId').value = btnEdit.dataset.id;
            document.getElementById('title').value = btnEdit.dataset.title;
            if (document.getElementById('parentSection')) {
                document.getElementById('parentSection').value = btnEdit.dataset.parent || '';
            }
            new bootstrap.Modal(sectionModal).show();
            return;
        }

        const pageLink = e.target.closest('.page-link');
        if (pageLink) {
            e.preventDefault();
            const pageAttr = pageLink.dataset.page;
            const page = pageAttr != null
                ? parseInt(pageAttr, 10)
                : (parseInt(pageLink.textContent, 10) - 1);

            if (!isNaN(page)) loadSections(page);
        }
    });

    const debouncedLoad = debounce(() => loadSections(0), 700);

    document.body.addEventListener('input', function (e) {
        const sectionInput = document.getElementById('searchSectionTitle');
        const parentInput = document.getElementById('searchParentSectionTitle');
        if (!sectionInput || !parentInput) return;

        if (e.target.id === 'searchSectionTitle') {
            parentInput.value = ''; //  پاک کردن parentSectionTitle وقتی sectionTitle تایپ شد
            debouncedLoad();
        }
        if (e.target.id === 'searchParentSectionTitle') {
            sectionInput.value = ''; //  پاک کردن sectionTitle وقتی parentSectionTitle تایپ شد
            debouncedLoad();
        }
    });


    document.body.addEventListener('change', (e) => {
        if (e.target.id === 'pageSize') {
            loadSections(0);
        }
    });
});
