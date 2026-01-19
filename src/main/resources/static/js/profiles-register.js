
// -------------------- Create Profile --------------------

async function handleCreateProfileSubmit(e) {
    e.preventDefault();
    clearValidationErrors();



    const profile = {
        firstName: document.getElementById('createFirstName').value.trim(),
        lastName: document.getElementById('createLastName').value.trim(),
        email: document.getElementById('createEmail').value.trim(),
        phone: document.getElementById('createPhone').value.trim(),
        username: document.getElementById('createUsername').value.trim(),
        password: document.getElementById('createPassword').value.trim()
    };

    let url = '/rest/profiles/register';


    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(profile)
        });

        const data = await handleResponse(response, 'create'); // مدیریت پاسخ با متد استاندارد


        // موفقیت
        bootstrap.Modal.getInstance(document.getElementById('profileRegisterModal')).hide();
        showToast('success', data.message || 'پروفایل با موفقیت ثبت شد');

    } catch (error) {
        // خطاهای اعتبارسنجی قبلاً در handleResponse نمایش داده شدند
        if (error.message !== 'Validation errors') {
            console.error('Profile creation error:', error);
            showToast('danger', error.message || 'خطا در ثبت پروفایل');
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


// -------------------- DOMContentLoaded --------------------
document.addEventListener('DOMContentLoaded', () => {


    const registerForm = document.getElementById('profileRegisterForm');
    if (registerForm) registerForm.addEventListener('submit', handleCreateProfileSubmit);

});
