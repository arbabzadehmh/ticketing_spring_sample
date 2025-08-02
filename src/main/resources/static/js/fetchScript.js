// function fetchScript(url, method) {
async function fetchData() {
    try{
        const response = await fetch('/ncustomers', {
            method: 'POST',
            body: JSON.stringify(
                {
                    firstName: 'ali',
                    lastName: 'alipour',
                    email: 'ali@gmail.com',
                    phone: '0123456789'
                }
            ),
            headers: {
                'Content-Type': 'application/json',
            }
        });

        console.log(response);
        console.log(response.body);

        if(!response.ok){
            throw new Error(response.statusText);
        }

        // const contentDiv = document.getElementById('contentDiv');
        // contentDiv.innerHTML = response.body;

        // todo : has error
        // const json = await response.json();
        // console.log("Info"  + JSON.stringify(json));
        console.log(response.text());

    }catch(error){
        console.log("Error" + error);
    }
}


// ${'btn'}.onclick(function(){
//     $.ajax({
//         url:'/',
//         method: 'GET',
//         success:function(response){
//
//         },
//         error:function(error){
//
//         }
//     })
// })