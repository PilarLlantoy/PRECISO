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
    else if(operacion=='Bulk1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Ejecución Exitosa!',
            text: 'El Traslado desde el servidor se realizo de forma correcta en el sistema.',
            showConfirmButton: true
        }).then((result) => {
            if (result.isConfirmed) {
                window.location.reload();
            }
        });
    }
    else if(operacion=='Bulk-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Ejecución Fallida!',
            text: 'No se encontro el fichero para la fecha deleccionada en la ruta.',
            showConfirmButton: true
        }).then((result) => {
            if (result.isConfirmed) {
                window.location.reload();
            }
        });
    }
}