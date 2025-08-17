function buildQuery(params) {
    const esc = encodeURIComponent;
    return Object.keys(params)
        .filter(k => params[k] !== null && params[k] !== undefined && params[k] !== '')
        .map(k => `${esc(k)}=${esc(params[k])}`)
        .join('&');
}

function loadTickets(page = 0, extraParams = {}) {
    const container = document.getElementById('tickets-table-container');
    const pageSizeElement = document.getElementById('pageSize') || { value: 10 };
    if (!container) return;

    const size = parseInt(pageSizeElement.value, 10) || 10;

    const baseParams = {
        page,
        size,
        fragment: true
    };
    const params = Object.assign({}, baseParams, extraParams);
    const url = `/tickets?${buildQuery(params)}`;

    fetch(url)
        .then(res => { if(!res.ok) throw new Error('خطا در دریافت تیکت‌ها'); return res.text(); })
        .then(html => {
            container.innerHTML = html;
            document.getElementById('pageSize').value = size;
            // فراخوانی دوباره init ها (مثل pagination, edit, delete)
            initTicketUi();
        })
        .catch(err => showToast('danger', err.message || 'خطا'));
}

function initTicketUi() {
    initPagination();
    initSortAndPageSize();
    initTicketEditButtons();
    initTicketDeleteButtons();

    // ثبت رویداد submit برای فرم Create
    const createForm = document.getElementById('ticketCreateForm');
    if (createForm) {
        createForm.addEventListener('submit', handleCreateTicketSubmit);
    }

    // ثبت رویداد submit برای فرم Edit
    const editForm = document.getElementById('ticketEditForm');
    if (editForm) {
        editForm.addEventListener('submit', handleEditTicketSubmit);
    }
}

// ================== Create Ticket ==================
async function handleCreateTicketSubmit(e) {
    e.preventDefault();
    clearValidationErrors();

    const ticket = {
        title: document.getElementById('createTitle').value.trim(),
        sectionId: document.getElementById('selectSection').value,
        content: document.getElementById('createContent').value.trim()
    };

    try {
        const response = await fetch('/tickets', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ticket)
        });

        const data = await handleResponse(response, 'create');

        bootstrap.Modal.getInstance(document.getElementById('ticketCreateModal')).hide();
        showToast('success', data.message || 'تیکت با موفقیت ثبت شد');
        loadTickets();

    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Ticket creation error:', error);
            showToast('danger', error.message || 'خطا در ثبت تیکت');
        }
    }
}

// ================== Edit Ticket ==================
async function handleEditTicketSubmit(e) {
    e.preventDefault();
    clearValidationErrors();

    const id = document.getElementById('editTicketId').value;
    const ticket = {
        title: document.getElementById('editTitle').value.trim(),
        sectionId: document.getElementById('editSection').value,
        status: document.getElementById('editStatus').value.trim()
    };

    try {
        const response = await fetch(`/tickets/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ticket)
        });

        const data = await handleResponse(response, 'edit');

        bootstrap.Modal.getInstance(document.getElementById('ticketEditModal')).hide();
        showToast('success', data.message || 'تیکت با موفقیت ویرایش شد');
        loadTickets();

    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Ticket edition error:', error);
            showToast('danger', error.message || 'خطا در ویرایش تیکت');
        }
    }
}

// ================== Delete Ticket ==================
async function handleTicketDelete(e) {
    const btn = e.target.closest('.btn-danger');
    const confirmText = btn.dataset.confirmText;
    if (!confirm(confirmText)) return;

    const id = btn.dataset.id;
    try {
        const response = await fetch(`/tickets/${id}`, { method: 'DELETE' });
        const data = await handleResponse(response, 'delete');

        showToast('success', data.message || 'تیکت با موفقیت حذف شد');
        loadTickets();

    } catch (error) {
        if (error.message !== 'Validation errors') {
            console.error('Ticket deletion error:', error);
            showToast('danger', error.message || 'خطا در حذف تیکت');
        }
    }
}

// ================== Init Buttons ==================
function initTicketEditButtons() {
    document.querySelectorAll('.btn-edit').forEach(btn => {
        btn.addEventListener('click', () => {
            const row = btn.closest('tr');
            document.getElementById('editTicketId').value = btn.dataset.id;
            document.getElementById('editTitle').value = row.cells[0].textContent.trim();
            document.getElementById('editSection').value = btn.dataset.sectionId;
            document.getElementById('editStatus').value = btn.dataset.status;

            new bootstrap.Modal(document.getElementById('ticketEditModal')).show();
        });
    });
}

// --------------------------------------------------------------
function initTicketDeleteButtons() {
    document.querySelectorAll('.btn-danger').forEach(btn => {
        btn.addEventListener('click', handleTicketDelete);
    });
}

// ----------------------------------------------------------------
/* submit handler modal */
// document.addEventListener('DOMContentLoaded', () => {
//     const form = document.getElementById('ticketFilterForm');
//     const modalEl = document.getElementById('ticketFilterModal');
//
//     if (form) {
//         form.addEventListener('submit', (e) => {
//             e.preventDefault();
//
//             const params = {
//                 dateFrom: document.getElementById('filterDateFrom').value,
//                 dateTo: document.getElementById('filterDateTo').value,
//                 status: document.getElementById('filterStatus').value,
//                 scoreLessThan: document.getElementById('filterScoreLessThan').value,
//                 customer: document.getElementById('filterCustomer').value,
//                 sectionId: document.getElementById('filterSection').value,
//                 title: document.getElementById('filterTitle').value
//             };
//
//             // حذف مقادیر خالی خودکار handled by buildQuery
//             loadTickets(0, params);
//
//             // بستن مودال
//             const modal = bootstrap.Modal.getInstance(modalEl);
//             if (modal) modal.hide();
//         });
//     }
//
//     const resetBtn = document.getElementById('filterResetBtn');
//     if (resetBtn) {
//         resetBtn.addEventListener('click', () => {
//             form.reset();
//         });
//     }
// });


document.addEventListener('DOMContentLoaded', () => {

    loadTickets();

    // === Ticket Filter Modal (GET) ===
    const ticketFilterForm = document.getElementById('ticketFilterForm');
    const ticketFilterModal = document.getElementById('ticketFilterModal');
    if (ticketFilterForm) {
        ticketFilterForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const params = {
                dateFrom: document.getElementById('filterDateFrom').value,
                dateTo: document.getElementById('filterDateTo').value,
                status: document.getElementById('filterStatus').value,
                scoreLessThan: document.getElementById('filterScoreLessThan').value,
                customer: document.getElementById('filterCustomer').value,
                sectionId: document.getElementById('filterSection').value,
                title: document.getElementById('filterTitle').value
            };

            loadTickets(0, params);
            const modal = bootstrap.Modal.getInstance(ticketFilterModal);
            if (modal) modal.hide();
        });
    }

    // === Ticket Create Modal (POST) ===
    const ticketCreateForm = document.getElementById('ticketCreateForm');
    if (ticketCreateForm) ticketCreateForm.addEventListener('submit', handleCreateTicketSubmit);

    // === Ticket Edit Modal (POST) ===
    const ticketEditForm = document.getElementById('ticketEditForm');
    if (ticketEditForm) ticketEditForm.addEventListener('submit', handleEditTicketSubmit);


    // === Optional: Reset buttons ===
    const filterResetBtn = document.getElementById('filterResetBtn');
    if (filterResetBtn && ticketFilterForm) {
        filterResetBtn.addEventListener('click', () => ticketFilterForm.reset());
    }


    initPagination();
    initSortAndPageSize();
    initTicketEditButtons();
    initTicketDeleteButtons();
});


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
function initPagination() {
    document.querySelectorAll('.page-link').forEach(link => {
        link.addEventListener('click', e => {
            e.preventDefault();
            const page = link.dataset.page;
            loadTickets(page); // اینجا صفحه را به تابع بده
        });
    });
}

// --------------------------------------------------
function initSortAndPageSize() {
    const pageSize = document.getElementById('pageSize');
    const sortBy = document.getElementById('sortBy');
    if (pageSize) pageSize.addEventListener('change', () => loadTickets(0));
    if (sortBy) sortBy.addEventListener('change', () => loadTickets(0));
}


