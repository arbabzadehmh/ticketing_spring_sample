async function submitForm(elementId, method, callbackFunction, eventType, url = null) {
    document.getElementById(elementId).addEventListener(eventType, async function (event) {
            event.preventDefault();
            const options = {method: method};

            if (event.target.tagName === "FORM") {
                var form = event.target;
                url = form.action;
                var formData = new FormData(event.target);
                var jsonObject = {};
                formData.forEach((value, key) => {
                    jsonObject[key] = value;
                });
                var jsonString = JSON.stringify(jsonObject);
                options.headers = {'Content-Type': 'application/json'}
                options.body = jsonString;
            }
            try{
            const response = await fetch(url, options);
            console.log("Mesage :"+ response.status);


            const data = await response.json();

            if (response.status === 200 || response.status === 201) {
                showToast("info", "Done");
                await callbackFunction(data.data);
            } else {
                showToast("error", data.message);                
                console.log("Error :" + JSON.stringify(data))
            }
        }catch(error){
            showToast("error", "Invalid URL !!!");
        }
        }
    );
}

function showToast(type, text) {
    var toastContainer = document.getElementById('toast-container');

    // Create the toast element
    var toast = document.createElement('div');
    toast.className = 'toast ' + type;
    toast.textContent = text;

    // Create the close button
    var closeButton = document.createElement('button');
    closeButton.className = 'close-btn';
    closeButton.innerHTML = '&times;';
    closeButton.onclick = function() {
        toast.className = toast.className.replace(' show', '');
        setTimeout(function() {
            toastContainer.removeChild(toast);
        }, 500);
    };

    // Append the close button to the toast
    toast.appendChild(closeButton);

    // Add the toast to the container
    toastContainer.appendChild(toast);

    // Trigger the animation
    setTimeout(function() {
        toast.className += ' show';
    }, 100);

    // Remove the toast after the duration
    setTimeout(function() {
        toast.className = toast.className.replace(' show', '');
        setTimeout(function() {
            if (toastContainer.contains(toast)) {
                toastContainer.removeChild(toast);
            }
        }, 500); // Match this value with the CSS transition duration
    }, 3000); // Duration of the toast
}