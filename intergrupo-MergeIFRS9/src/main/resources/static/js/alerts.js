function validateAlerts(operacion,data)
{
    if(operacion=='Modify1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Modificación exitosa!',
            text: 'El registro "'+data+'" fue actualizado de forma correcta en el sistema.',
            showConfirmButton: true
        })
    }
    else if(operacion=='Add1')
     {
         Swal.fire({
             position: 'center',
             icon: 'success',
             title: '¡Adición exitosa!',
             text: 'El registro "'+data+'" fue agregado de forma correcta en el sistema.',
             showConfirmButton: true
         })
     }
}