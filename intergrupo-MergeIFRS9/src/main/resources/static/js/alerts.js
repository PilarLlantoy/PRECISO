function dialogOpen()
{
    Swal.fire({
        title: "Cargando...",
        width: 600,
        padding: "3em",
        color: "#0b5ed7",
        background: "#fff",
        allowOutsideClick: false, // Evitar que se cierre al hacer clic fuera
        showConfirmButton: false, // Ocultar el botón de confirmación
        backdrop: `
          rgba(0,0,0,0.3)
          left top
          no-repeat
        `,
        didOpen: () => {
            Swal.showLoading(); // Mostrar animación de carga
        }
    });
}
function validateAlerts(operacion,data)
{
    if(operacion=='Modify1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Modificación exitosa!',
            text: 'El registro fue actualizado de forma correcta en el sistema.',
            showConfirmButton: true
        })
    }
    else if(operacion=='Add1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Adición exitosa!',
            text: 'El registro fue agregado de forma correcta en el sistema.',
            showConfirmButton: true
        })
    }
    else if(operacion=='Date-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Falló Fecha Cierre!',
            text: 'Esta fecha ya se encuentra almacenada.',
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
            showConfirmButton: true,
            allowOutsideClick: false
        }).then((result) => {
            if (result.isConfirmed) {
                const period = document.getElementById('period').value;
                const arhcont = document.getElementById('arhcont').value;
                window.location.href = window.location.origin + window.location.pathname + `?period=${encodeURIComponent(period)}&arhcont=${encodeURIComponent(arhcont)}`
            }
        });
    }
    else if(operacion=='Bulk2')
        {
            Swal.fire({
                position: 'center',
                icon: 'success',
                title: '¡Ejecución Exitosa!',
                text: 'El Traslado desde el servidor se realizo de forma correcta en el sistema.',
                showConfirmButton: true,
                allowOutsideClick: false
            }).then((result) => {
                if (result.isConfirmed) {
                    const period = document.getElementById('period').value;
                    const arhcont = document.getElementById('arhcont').value;
                    const concil = document.getElementById('selectedConciliacion').value;
                    window.location.href = window.location.origin + window.location.pathname + `?period=${encodeURIComponent(period)}&selectedConciliacion=${encodeURIComponent(concil)}&arhcont=${encodeURIComponent(arhcont)}`
                }
            });
        }
    else if(operacion=='Bulk-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Ejecución Fallida!',
            text: 'No se pudo procesar el cargue, valide el log de detalle.',
            showConfirmButton: true,
            allowOutsideClick: false
        }).then((result) => {
            if (result.isConfirmed) {
                const period = document.getElementById('period').value;
                const arhcont = document.getElementById('arhcont').value;
                window.location.href = window.location.origin + window.location.pathname + `?period=${encodeURIComponent(period)}&arhcont=${encodeURIComponent(arhcont)}`;
            }
        });
    }
    else if(operacion=='Bulk->1')
        {
            Swal.fire({
                position: 'center',
                icon: 'success',
                title: '¡Ejecución Exitosa!',
                text: 'La generación de cuentas se realizo de forma correcta en el sistema.',
                showConfirmButton: true,
                allowOutsideClick: false
            }).then((result) => {
                if (result.isConfirmed) {
                    const period = document.getElementById('period').value;
                    const arhcont = document.getElementById('arhcont').value;
                    const evento = document.getElementById('evento').value;
                    window.location.href = window.location.origin + window.location.pathname + `?period=${encodeURIComponent(period)}&arhcont=${encodeURIComponent(arhcont)}&evento=${encodeURIComponent(evento)}`;
                }
            });
        }
    else if(operacion=='Bulk->2')
            {
                Swal.fire({
                    position: 'center',
                    icon: 'error',
                    title: '¡Ejecución Fallida!',
                    text: 'No se pudo procesar el cargue, valide el log de detalle.',
                    showConfirmButton: true,
                    allowOutsideClick: false
                }).then((result) => {
                    if (result.isConfirmed) {
                        const period = document.getElementById('period').value;
                        const arhcont = document.getElementById('arhcont').value;
                        const evento = document.getElementById('evento').value;
                        window.location.href = window.location.origin + window.location.pathname + `?period=${encodeURIComponent(period)}&arhcont=${encodeURIComponent(arhcont)}&evento=${encodeURIComponent(evento)}`;
                    }
                });
            }
    else if(operacion=='Bulk-2')
        {
            Swal.fire({
                position: 'center',
                icon: 'error',
                title: '¡Ejecución Fallida!',
                text: 'No se pudo procesar el cargue, valide el log de detalle.',
                showConfirmButton: true,
                allowOutsideClick: false
            }).then((result) => {
                if (result.isConfirmed) {
                    const period = document.getElementById('period').value;
                    const arhcont = document.getElementById('arhcont').value;
                    const concil = document.getElementById('selectedConciliacion').value;
                    window.location.href = window.location.origin + window.location.pathname + `?period=${encodeURIComponent(period)}&selectedConciliacion=${encodeURIComponent(concil)}&arhcont=${encodeURIComponent(arhcont)}`;
                }
            });
        }
}