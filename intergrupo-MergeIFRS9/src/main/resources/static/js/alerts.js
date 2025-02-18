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
    else if(operacion=='Maes1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Generación Exitosa!',
            text: 'Se generaron los registros de las fechas de maestro de forma correcta.',
            showConfirmButton: true
        })
    }
    else if(operacion=='Maes-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Generación Fallida!',
            text: 'Se presento un error al generar las fechas.',
            showConfirmButton: true
        })
    }
    else if(operacion=='Maes-2')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Registro Duplicado!',
            text: 'No se pudo insertar el registro, revise la información ingresada.',
            showConfirmButton: true
        })
    }
    else if(operacion=='CM1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Carga Masiva Ejecutada!',
            text: 'Se ha finalizado el proceso de carga masiva de los inventarios.',
            showConfirmButton: true
        })
    }
    else if(operacion=='CM-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Inventario No Seleccionado!',
            text: 'Debe seleccionar al menos un inventario para ejecutar.',
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
    else if(operacion=='Bulk->3')
        {
            Swal.fire({
                position: 'center',
                icon: 'success',
                title: '¡Ejecución Exitosa!',
                text: 'La generación de cuentas ya ha sido aprobada anteriormente.',
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
    else if(operacion=='Bulk--1')
        {
            Swal.fire({
                position: 'center',
                icon: 'success',
                title: '¡Ejecución Exitosa!',
                text: 'La generación de conciliacón se realizó de forma correcta en el sistema.',
                showConfirmButton: true,
                allowOutsideClick: false
            }).then((result) => {
                if (result.isConfirmed) {
                    const period = document.getElementById('period').value;
                    const period2 = document.getElementById('period2').value;
                    const arhcont = document.getElementById('arhcont').value;
                    window.location.href = window.location.origin + window.location.pathname + `?arhcont=${encodeURIComponent(arhcont)}&period=${encodeURIComponent(period)}&period2=${encodeURIComponent(period2)}`;
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
    else if(operacion=='Bulk--2')
        {
            Swal.fire({
                position: 'center',
                icon: 'error',
                title: '¡Ejecución Fallida!',
                text: 'No se pudo procesar la conciliación, valide el log de detalle.',
                showConfirmButton: true,
                allowOutsideClick: false
            }).then((result) => {
                if (result.isConfirmed) {
                    const period = document.getElementById('period').value;
                    const period2 = document.getElementById('period2').value;
                    const arhcont = document.getElementById('arhcont').value;
                    window.location.href = window.location.origin + window.location.pathname + `?arhcont=${encodeURIComponent(arhcont)}&period=${encodeURIComponent(period)}&period2=${encodeURIComponent(period2)}`;
                }
            });
        }
}