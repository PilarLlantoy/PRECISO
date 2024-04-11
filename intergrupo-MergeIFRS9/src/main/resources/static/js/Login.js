/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

let user = document.querySelector('#username');
let password = document.querySelector('#password');
let form = document.querySelector('#validar');

document.addEventListener('DOMContentLoaded', () => {
    validateFormFields();
});

function validateFormFields() {
    form.addEventListener('submit', () => {
        if (user.value == '' || password.value == '') {
            p = document.createElement('p')
            p.textContent = 'Todos los campos son requeridos'
            form.appendChild(p)
        }
    })
}

