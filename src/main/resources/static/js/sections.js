function loadSections(page = 0) {
    const container = document.getElementById('sections-table-container');
    const pageSizeElement = document.getElementById('pageSize');

    const sectionTitleInput = document.getElementById('searchSectionTitle');
    const parentSectionTitleInput = document.getElementById('searchParentSectionTitle');

    if (!container || !pageSizeElement) {
        console.log('%cSections table not found, skipping loadSections()', 'color: orange;');
        return;
    }

    const size = parseInt(pageSizeElement.value, 10);

    const sectionTitle = sectionTitleInput ? sectionTitleInput.value.trim() : '';
    const parentSectionTitle = parentSectionTitleInput ? parentSectionTitleInput.value.trim() : '';

    let url = `/sections?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}&fragment=true`;

    if (sectionTitle) {
        url += `&sectionTitle=${encodeURIComponent(sectionTitle)}`;
    } else if (parentSectionTitle) {
        url += `&parentSectionTitle=${encodeURIComponent(parentSectionTitle)}`;
    }

    fetch(url)
        .then(response => {
            if (!response.ok) throw new Error('خطا در دریافت داده‌ها');
            return response.text();
        })
        .then(html => {
            container.innerHTML = html;
            document.getElementById('pageSize').value = size;

            initPagination();
            initSortAndPageSize();
            initDeleteButtons();
            initEditButtons();
            initAddButtons();
            initSearchInputs();
        })
        .catch(error => showToast('danger', error.message || 'خطا در دریافت داده‌ها'));
}

// ----------------------------------------------------
async function handleSectionSubmit(e) {
    e.preventDefault();
    clearValidationErrors();

    const id = document.getElementById('sectionId').value;
    console.log("Section ID on submit:", id);
    const parentSectionId = document.getElementById('parentSection').value.trim();
    const section = {
        title: document.getElementById('title').value.trim(),
        parentSection: parentSectionId ? { id: parentSectionId } : null
    };

    const url = id ? `/sections/${id}` : '/sections';
    const method = id ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, {
            method,
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(section)
        });

        const data = await handleResponse(response, id ? 'edit' : 'create');

        bootstrap.Modal.getInstance(document.getElementById('sectionModal')).hide();
        showToast('success', data.message || (id ? 'دپارتمان ویرایش شد' : 'دپارتمان ثبت شد'));
        loadSections();
    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Section save error:', error);
            showToast('danger', error.message || 'خطا در ذخیره دپارتمان');
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
async function initEditButtons() {
    document.querySelectorAll('.btn-edit').forEach(btn => {
        btn.addEventListener('click', async () => {
            const modalTitle = document.querySelector('#sectionModalLabel');
            modalTitle.textContent = modalTitle.dataset.titleEdit;

            const sectionId = btn.dataset.id;
            const sectionTitle = btn.dataset.title;
            const parentId = btn.dataset.parentId || '';

            document.getElementById('sectionId').value = sectionId;
            document.getElementById('title').value = sectionTitle;

            // دریافت لیست والدهای مجاز
            const response = await fetch(`/rest/sections?id=${sectionId}`);
            if (!response.ok) {
                showToast('danger', 'خطا در دریافت لیست دپارتمان‌ها');
                return;
            }
            const parents = await response.json();

            const parentSelect = document.getElementById('parentSection');
            parentSelect.innerHTML = `<option value="">بدون دپارتمان بالادستی</option>`;
            parents.forEach(p => {
                const option = document.createElement('option');
                option.value = p.id;
                option.textContent = p.title;
                parentSelect.appendChild(option);
            });

            // ست کردن والد انتخاب‌شده
            parentSelect.value = parentId;

            new bootstrap.Modal(document.getElementById('sectionModal')).show();
        });
    });
}

// -------------------------------------------------------
async function initAddButtons() {
    const addSectionButton = document.querySelector('[data-bs-target="#sectionModal"]');
    if (addSectionButton) {
        addSectionButton.addEventListener('click', async () => {
            const modalTitle = document.getElementById('sectionModalLabel');
            modalTitle.textContent = modalTitle.dataset.titleAdd;

            document.getElementById('sectionId').value = '';
            document.getElementById('title').value = '';

            // دریافت لیست کامل سکشن‌ها
            const response = await fetch(`/rest/sections`);
            if (!response.ok) {
                showToast('danger', 'خطا در دریافت لیست دپارتمان‌ها');
                return;
            }
            const parents = await response.json();

            const parentSelect = document.getElementById('parentSection');
            parentSelect.innerHTML = `<option value="">بدون دپارتمان بالادستی</option>`;
            parents.forEach(p => {
                const option = document.createElement('option');
                option.value = p.id;
                option.textContent = p.title;
                parentSelect.appendChild(option);
            });

            parentSelect.value = '';
        });
    }
}


// -------------------------------------------------------
async function handleSectionDelete(e) {
    const btn = e.target.closest('.btn-danger');
    const confirmText = btn.dataset.confirmText;
    if (!confirm(confirmText)) return;

    const id = btn.dataset.id;

    try {
        const response = await fetch(`/sections/${id}`, {method: 'DELETE'});
        const data = await handleResponse(response, 'delete');
        showToast('success', data.message || 'دپارتمان حذف شد');
        loadSections();
    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Section deletion error:', error);
            showToast('danger', error.message || 'خطا در حذف دپارتمان');
        }
    }
}

// -----------------------------------------------------
function initDeleteButtons() {
    document.querySelectorAll('.btn-danger').forEach(btn => btn.addEventListener('click', handleSectionDelete));
}

// -----------------------------------------------------
function initPagination() {
    document.querySelectorAll('.section-page-link').forEach(link => {
        link.addEventListener('click', e => {
            e.preventDefault();
            const page = link.dataset.page;
            loadSections(page);
        });
    });
}

// --------------------------------------------------
function initSortAndPageSize() {
    const pageSize = document.getElementById('pageSize');
    if (pageSize) pageSize.addEventListener('change', () => loadSections(0));
}

// ---------------------------------------------------
function debounce(fn, delay) {
    let timeoutId;
    return function(...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => fn.apply(this, args), delay);
    };
}

// ------------------------------------------------------------

function initSearchInputs() {
    const sectionTitleInput = document.getElementById('searchSectionTitle');
    const parentSectionTitleInput = document.getElementById('searchParentSectionTitle');

    const debouncedLoad = debounce(() => loadSections(0), 700);

    if (sectionTitleInput && parentSectionTitleInput) {
        sectionTitleInput.addEventListener('input', () => {
            if (sectionTitleInput.value.trim() !== '') {
                parentSectionTitleInput.value = '';
            }
            debouncedLoad();
        });

        parentSectionTitleInput.addEventListener('input', () => {
            if (parentSectionTitleInput.value.trim() !== '') {
                sectionTitleInput.value = '';
            }
            debouncedLoad();
        });
    }
}


// ----------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
    loadSections();

    const form = document.getElementById('sectionForm');
    if (form) form.addEventListener('submit', handleSectionSubmit);

    const modalTitle = document.getElementById('sectionModalLabel');

    // دکمه افزودن دسترسی
    const addSectionButton = document.querySelector('[data-bs-target="#sectionModal"]');
    if (addSectionButton) {
        addSectionButton.addEventListener('click', () => {
            modalTitle.textContent = modalTitle.dataset.titleAdd; // پیش‌فرض
            document.getElementById('sectionId').value = '';
            document.getElementById('title').value = '';
        });
    }

    // دکمه‌های ویرایش
    initEditButtons();
    initAddButtons();
    initPagination();
    initSortAndPageSize();
    initDeleteButtons();

    //  برگرداندن عنوان به حالت پیش‌فرض بعد از بسته شدن مودال
    document.getElementById('sectionModal').addEventListener('hide.bs.modal', () => {
        modalTitle.textContent = modalTitle.dataset.titleAdd;
        const focusedElement = document.querySelector('#sectionModal :focus');
        if (focusedElement) focusedElement.blur();
    });

    //  گوش دادن به event باز شدن مودال برای اطمینان
    document.getElementById('sectionModal').addEventListener('show.bs.modal', (event) => {
        const modalTitle = document.getElementById('sectionModalLabel');

        // اگر دکمه بازکننده مودال دارای کلاس btn-add بود، یعنی افزودن است
        if (event.relatedTarget && event.relatedTarget.classList.contains('btn-add')) {
            modalTitle.textContent = modalTitle.dataset.titleAdd;
            document.getElementById('sectionId').value = '';
            document.getElementById('title').value = '';
        }
    });

});

