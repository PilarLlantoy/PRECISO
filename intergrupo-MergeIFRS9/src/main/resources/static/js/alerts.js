function operationDelete(operation)
{
    Swal.fire({
      title: '¿Está seguro?',
      text: '¡Se borrarán TODOS los datos de la tabla!',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Sí, Limpiar tabla!',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (result.isConfirmed) {
          if(operation=="third")
          {
            Swal.fire(
              '¡Elementos borrados!',
              'La tabla de ha sido limpiada.',
              'success'
            );
            $.ajax({
                url:'/parametric/clearThird',
                type:'GET'}
              );
              setTimeout(function(){
                  window.location.href="/parametric/third";
              },2000);
          }
          else if(operation=="signature")
          {
              Swal.fire(
                  '¡Elementos borrados!',
                  'La tabla de ha sido limpiada.',
                  'success'
              );
              $.ajax({
                  url:'/parametric/clearSignature',
                  type:'GET'}
              );
              setTimeout(function(){
                  window.location.href="/parametric/signature";
              },2000);
          }
          else if(operation=="baseicrv")
          {
              Swal.fire(
                  '¡Elementos borrados!',
                  'La tabla de ha sido limpiada.',
                  'success'
              );
              $.ajax({
                  url:'/briefcase/clearBaseicrv',
                  type:'GET'}
              );
              setTimeout(function(){
                  window.location.href="/briefcase/baseicrv";
              },2000);
          }
          else if(operation=="contactosicrv")
          {
              Swal.fire(
                  '¡Elementos borrados!',
                  'La tabla de ha sido limpiada.',
                  'success'
              );
              $.ajax({
                  url:'/briefcase/clearContactosicrv',
                  type:'GET'}
              );
              setTimeout(function(){
                  window.location.href="/briefcase/contactosicrv";
              },2000);
          }
          else if(operation=="informe")
          {
              Swal.fire(
                  '¡Elementos borrados!',
                  'La tabla de ha sido limpiada.',
                  'success'
              );
              $.ajax({
                  url:'/parametric/clearInforme',
                  type:'GET'}
              );
              setTimeout(function(){
                  window.location.href="/parametric/informe";
              },2000);
          }
          else if(operation=="informeNotas")
          {
              Swal.fire(
                  '¡Elementos borrados!',
                  'La tabla de ha sido limpiada.',
                  'success'
              );
              $.ajax({
                  url:'/parametric/clearInformeNotas',
                  type:'GET'}
              );
              setTimeout(function(){
                  window.location.href="/parametric/informeNotas";
              },2000);
          }
          else if(operation=="mda")
          {
              Swal.fire(
                  '¡Elementos borrados!',
                  'La tabla de ha sido limpiada.',
                  'success'
              );
              $.ajax({
                  url:'/parametric/clearMda',
                  type:'GET'}
              );
              setTimeout(function(){
                  window.location.href="/parametric/mda";
              },2000);
          }
          else if(operation=="anexo8finrep")
        {
            Swal.fire(
                '¡Elementos borrados!',
                'La tabla de ha sido limpiada.',
                'success'
            );
            $.ajax({
                url:'/reports/clearAnexo8finrep',
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/reports/anexo8finrep";
            },2000);
        }
          else if(operation=="accountBanco")
          {
              Swal.fire(
                  '¡Elementos borrados!',
                  'La tabla de ha sido limpiada.',
                  'success'
              );
              $.ajax({
                  url:'/parametric/clearAccountBanco',
                  type:'GET'}
              );
              setTimeout(function(){
                  window.location.href="/parametric/accountBanco";
              },2000);
          }
          else if(operation=="pFechas")
          {
              Swal.fire(
                  '¡Elementos borrados!',
                  'La tabla de ha sido limpiada.',
                  'success'
              );
              $.ajax({
                  url:'/parametric/clearFechas',
                  type:'GET'}
              );
              setTimeout(function(){
                  window.location.href="/parametric/fechas";
              },2000);
          }
          else if(operation=="pNic34")
          {
              Swal.fire(
                  '¡Elementos borrados!',
                  'La tabla de ha sido limpiada.',
                  'success'
              );
              $.ajax({
                  url:'/parametric/clearNic34',
                  type:'GET'}
              );
              setTimeout(function(){
                  window.location.href="/parametric/nic34";
              },2000);
          }
          else if(operation=="pNic34Consol")
          {
              Swal.fire(
                  '¡Elementos borrados!',
                  'La tabla de ha sido limpiada.',
                  'success'
              );
              $.ajax({
                  url:'/parametric/clearNic34Consol',
                  type:'GET'}
              );
              setTimeout(function(){
                  window.location.href="/parametric/nic34Consol";
              },2000);
          }
          else if(operation=="accountCc")
          {
              Swal.fire(
                  '¡Elementos borrados!',
                  'La tabla de ha sido limpiada.',
                  'success'
              );
              $.ajax({
                  url:'/accountsReceivable/clearAccountCc',
                  type:'GET'}
              );
              setTimeout(function(){
                  window.location.href="/accountsReceivable/accountCc";
              },2000);
          }
          else if(operation=="accountCc12")
          {
              Swal.fire(
                  '¡Elementos borrados!',
                  'La tabla de ha sido limpiada.',
                  'success'
              );
              $.ajax({
                  url:'/eeffConsolidated/clearAccountParametric',
                  type:'GET'}
              );
              setTimeout(function(){
                  window.location.href="/eeffConsolidated/parametricsConsolidated";
              },2000);
          }

          else if(operation=="accountCc15")
          {
              Swal.fire(
                  '¡Elementos borrados!',
                  'La tabla de ha sido limpiada.',
                  'success'
              );
              $.ajax({
                  url:'/eeffConsolidated/clearAccountParametric1',
                  type:'GET'}
              );
              setTimeout(function(){
                  window.location.href="/eeffConsolidated/parametricsAjustesMinimos";
              },2000);
          }

          else if(operation=="accountCcMayores")
          {
              Swal.fire(
                  '¡Elementos borrados!',
                  'La tabla de ha sido limpiada.',
                  'success'
              );
              $.ajax({
                  url:'/eeffConsolidated/clearAccountParametricMayores',
                  type:'GET'}
              );
              setTimeout(function(){
                  window.location.href="/eeffConsolidated/parametricsAjustesMayores";
              },2000);
          }


          else if(operation=="thirdsCc")
          {
              Swal.fire(
                  '¡Elementos borrados!',
                  'La tabla de ha sido limpiada.',
                  'success'
              );
              $.ajax({
                  url:'/parametric/clearThirdsCc',
                  type:'GET'}
              );
              setTimeout(function(){
                  window.location.href="/parametric/thirdsCc";
              },2000);
          }
          else if(operation=="yntp")
          {
            var jqxhr = $.get( '/parametric/clearYntp', function(data) {
               if(data==false)
               {
                 window.location.href="/parametric/yntp?resp=UpdateCascade-3";
               }
               else
               {
                 window.location.href="/parametric/yntp?resp=Delete1";
               }
             });
          }
          else if(operation=="entity")
          {
              var jqxhr = $.get( '/parametric/clearTypeEntity', function(data) {
                  if(data==false)
                  {
                      window.location.href="/parametric/typeEntity?resp=UpdateCascade-3";
                  }
                  else
                  {
                      window.location.href="/parametric/typeEntity?resp=Delete1";
                  }
              });
          }
          else if(operation=="currency")
          {
             var jqxhr = $.get( '/parametric/clearCurrency', function(data) {
               if(data==false)
               {
                 window.location.href="/parametric/currency?resp=UpdateCascade-2";
               }
               else
               {
                 window.location.href="/parametric/currency?resp=Delete1";
               }
             });
          }
          else if(operation=="responsibleAccount")
            {
               var jqxhr = $.get( '/parametric/clearResponsibleAccount', function(data) {
                 if(data==false)
                 {
                   window.location.href="/parametric/responsibleAccount?resp=UpdateCascade-1";
                 }
                 else
                 {
                   window.location.href="/parametric/responsibleAccount?resp=Delete1";
                 }
               });
            }
            else if(operation=="country")
            {
               var jqxhr = $.get( '/parametric/clearCountry', function(data) {
                 if(data==false)
                 {
                   window.location.href="/parametric/country?resp=UpdateCascade-4";
                 }
                 else
                 {
                   window.location.href="/parametric/country?resp=Delete1";
                 }
               });
            }
            else if(operation=="rejectId")
            {
               var jqxhr = $.get( '/ifrs/clearRejectIdP1', function(data) {
                 if(data==false)
                 {
                   window.location.href="/ifrs/rejectIdP1?resp=UpdateCascade-1";
                 }
                 else
                 {
                   window.location.href="/ifrs/rejectIdP1?resp=Delete1";
                 }
               });
            }
          else if(operation=="rejectIdP2")
          {
              var jqxhr = $.get( '/ifrs/clearRejectIdP2', function(data) {
                  if(data==false)
                  {
                      window.location.href="/ifrs/rejectIdP2?resp=UpdateCascade-1";
                  }
                  else
                  {
                      window.location.href="/ifrs/rejectIdP2?resp=Delete1";
                  }
              });
          }
          else if(operation=="equivalences")
          {
              var jqxhr = $.get( '/ifrs/clearEquivalences', function(data) {
                  if(data==false)
                  {
                      window.location.href="/ifrs/equivalences?resp=UpdateCascade-1";
                  }
                  else
                  {
                      window.location.href="/ifrs/equivalences?resp=Delete1";
                  }
              });
          }
          else if(operation=="accountCreateC")
          {
              var jqxhr = $.get( '/ifrs/clearAccountCreation', function(data) {
                  if(data==false)
                  {
                      window.location.href="/ifrs/accountCreation?resp=UpdateCascade-1";
                  }
                  else
                  {
                      window.location.href="/ifrs/accountCreation?resp=Delete1";
                  }
              });
          }
          else if(operation=="accountControl")
          {
              var jqxhr = $.get( '/ifrs/clearAccountControl', function(data) {
                  if(data==false)
                  {
                      window.location.href="/ifrs/accountControl?resp=UpdateCascade-1";
                  }
                  else
                  {
                      window.location.href="/ifrs/accountControl?resp=Delete1";
                  }
              });
          }
          else if(operation.includes('riskAccountLoad:'))
          {
              var jqxhr = $.get( '/ifrs/clearRiskAccountLoad?period='+operation, function(data) {
                  if(data==false)
                  {
                      window.location.href="/ifrs/riskAccountLoad?resp=UpdateCascade-1&period="+operation.split(':')[1];
                  }
                  else
                  {
                      window.location.href="/ifrs/riskAccountLoad?resp=Delete1&period="+operation.split(':')[1];
                  }
              });
          }
          else if(operation=="planeRistras")
          {
              var jqxhr = $.get( '/ifrs/clearPlaneRistras', function(data) {
                  if(data==false)
                  {
                      window.location.href="/ifrs/planeRistras?resp=UpdateCascade-1";
                  }
                  else
                  {
                      window.location.href="/ifrs/planeRistras?resp=Delete1";
                  }
              });
          }
            else if(operation=="subsidiaries")
             {
                var jqxhr = $.get( '/parametric/clearSubsidiaries', function(data) {
                  if(data==false)
                  {
                    window.location.href="/parametric/subsidiaries?resp=UpdateCascade-1";
                  }
                  else
                  {
                    window.location.href="/parametric/subsidiaries?resp=Delete1";
                  }
                });
             }
             else if(operation=="quotas")
            {
              var jqxhr = $.get( '/ifrs/clearQuota', function(data) {
                   if(data==false)
                   {
                     window.location.href="/ifrs/quotas?resp=UpdateCascade-1";
                   }
                   else
                   {
                     window.location.href="/ifrs/quotas?resp=Delete1";
                   }
                 });
              }
          else if(operation=="center")
          {
              var jqxhr = $.get( '/ifrs/clearCenter', function(data) {
                  if(data==false)
                  {
                      window.location.href="/ifrs/center?resp=UpdateCascade-1";
                  }
                  else
                  {
                      window.location.href="/ifrs/center?resp=Delete1";
                  }
              });
          }
             else if(operation=="contract")
              {
                 var jqxhr = $.get( '/parametric/clearContract', function(data) {
                   if(data==false)
                   {
                     window.location.href="/parametric/contract?resp=UpdateCascade-1";
                   }
                   else
                   {
                     window.location.href="/parametric/contract?resp=Delete1";
                   }
                 });
              }
              else if(operation=="indicators")
            {
               var jqxhr = $.get( '/parametric/clearIndicators', function(data) {
                 if(data==false)
                 {
                   window.location.href="/parametric/indicators?resp=UpdateCascade-1";
                 }
                 else
                 {
                   window.location.href="/parametric/indicators?resp=Delete1";
                 }
               });
            }
            else if(operation=="reclassification")
            {
               var jqxhr = $.get( '/parametric/clearReclassification', function(data) {
                 if(data==false)
                 {
                   window.location.href="/parametric/reclassification?resp=UpdateCascade-1";
                 }
                 else
                 {
                   window.location.href="/parametric/reclassification?resp=Delete1";
                 }
               });
            }
          else if(operation=="rejections")
          {
              var jqxhr = $.get( '/ifrs/deleteReject', function(data) {
                  if(data==false)
                  {
                      window.location.href="/ifrs/rejectionsCc?resp=UpdateCascade-1";
                  }
                  else
                  {
                      window.location.href="/ifrs/rejectionsCc?resp=Delete1";
                  }
              });
          }
            else if(operation=="provisions")
            {
               var jqxhr = $.get( '/parametric/clearProvisions', function(data) {
                 if(data==false)
                 {
                   window.location.href="/parametric/provisions?resp=UpdateCascade-1";
                 }
                 else
                 {
                   window.location.href="/parametric/provisions?resp=Delete1";
                 }
               });
            }
            else if(operation=="neocon")
            {
               var jqxhr = $.get( '/parametric/clearNeocon', function(data) {
                 if(data==false)
                 {
                   window.location.href="/parametric/neocon?resp=UpdateCascade-1";
                 }
                 else
                 {
                   window.location.href="/parametric/neocon?resp=Delete1";
                 }
               });
            }
            else if(operation=="rp21")
            {
               var jqxhr = $.get( '/reports/clearRp21', function(data) {
                 if(data==false)
                 {
                   window.location.href="/reports/rp21?resp=UpdateCascade-1";
                 }
                 else
                 {
                   window.location.href="/reports/rp21?resp=Delete1";
                 }
               });
            }
          else if(operation=="aval")
          {
              var jqxhr = $.get( '/parametric/clearAval', function(data) {
                  if(data==false)
                  {
                      window.location.href="/parametric/avalType?resp=UpdateCascade-1";
                  }
                  else
                  {
                      window.location.href="/parametric/avalType?resp=Delete1";
                  }
              });
          }
          else if(operation=="change")
          {
              var jqxhr = $.get( '/parametric/clearChangeCurrency', function(data) {
                  if(data==false)
                  {
                      window.location.href="/parametric/changeCurrency?resp=UpdateCascade-1";
                  }
                  else
                  {
                      window.location.href="/parametric/changeCurrency?resp=Delete1";
                  }
              });
          }
          else if(operation=="operation")
          {
              var jqxhr = $.get( '/parametric/clearOperation', function(data) {
                  if(data==false)
                  {
                      window.location.href="/parametric/operationAccount?resp=UpdateCascade-1";
                  }
                  else
                  {
                      window.location.href="/parametric/operationAccount?resp=Delete1";
                  }
              });
          }
          else if(operation=="rysParametric")
          {
              var jqxhr = $.get( '/ifrs/clearRysParametric', function(data) {
                  if(data==false)
                  {
                      window.location.href="/ifrs/rysParametric?resp=UpdateCascade-1";
                  }
                  else
                  {
                      window.location.href="/ifrs/rysParametric?resp=Delete1";
                  }
              });
          }
          else if(operation=="garantBank")
        {
            var jqxhr = $.get( '/parametric/clearGarantBank', function(data) {
                if(data==false)
                {
                    window.location.href="/parametric/garantBank?resp=UpdateCascade-1";
                }
                else
                {
                    window.location.href="/parametric/garantBank?resp=Delete1";
                }
            });
        }
        else if(operation=="comerParametric")
        {
            var jqxhr = $.get( '/parametric/clearComerParametric', function(data) {
                if(data==false)
                {
                    window.location.href="/parametric/comerParametric?resp=UpdateCascade-1";
                }
                else
                {
                    window.location.href="/parametric/comerParametric?resp=Delete1";
                }
            });
        }
        else if(operation=="genericAccount")
        {
            var jqxhr = $.get( '/parametric/clearGenericAccount', function(data) {
                if(data==false)
                {
                    window.location.href="/parametric/genericAccount?resp=UpdateCascade-3";
                }
                else
                {
                    window.location.href="/parametric/genericAccount?resp=Delete1";
                }
            });
        }
        else if(operation=="reposYSimultaneas")
        {
            var jqxhr = $.get( '/parametric/clearReposYSimultaneas', function(data) {
                if(data==false)
                {
                    window.location.href="/parametric/reposYSimultaneas?resp=UpdateCascade-3";
                }
                else
                {
                    window.location.href="/parametric/reposYSimultaneas?resp=Delete1";
                }
            });
        }
          else if(operation=="counterpartyGenericContracts")
          {
              var jqxhr = $.get( '/parametric/clearCounterpartyGenericContracts', function(data) {
                  if(data==false)
                  {
                      window.location.href="/parametric/counterpartyGenericContracts?resp=UpdateCascade-3";
                  }
                  else
                  {
                      window.location.href="/parametric/counterpartyGenericContracts?resp=Delete1";
                  }
              });
          }
          else if(operation=="accountHistoryIFRS9")
          {
              var jqxhr = $.get( '/parametric/clearAccountHistoryIFRS9', function(data) {
                  if(data==false)
                  {
                      window.location.href="/parametric/accountHistoryIFRS9?resp=UpdateCascade-3";
                  }
                  else
                  {
                      window.location.href="/parametric/accountHistoryIFRS9?resp=Delete1";
                  }
              });
          }
          else if(operation=="accountAndByProduct")
          {
              var jqxhr = $.get( '/parametric/clearAccountAndByProduct', function(data) {
                  if(data==false)
                  {
                      window.location.href="/parametric/accountAndByProduct?resp=UpdateCascade-3";
                  }
                  else
                  {
                      window.location.href="/parametric/accountAndByProduct?resp=Delete1";
                  }
              });
          }
          else if(operation=="provisionsAndProduct")
          {
              var jqxhr = $.get( '/parametric/clearProvisionsAndProduct', function(data) {
                  if(data==false)
                  {
                      window.location.href="/parametric/provisionsAndProduct?resp=UpdateCascade-3";
                  }
                  else
                  {
                      window.location.href="/parametric/provisionsAndProduct?resp=Delete1";
                  }
              });
          }
          else if(operation=="segmentDecisionTree")
          {
              var jqxhr = $.get( '/parametric/clearSegmentDecisionTree', function(data) {
                  if(data==false)
                  {
                      window.location.href="/parametric/segmentDecisionTree?resp=UpdateCascade-3";
                  }
                  else
                  {
                      window.location.href="/parametric/segmentDecisionTree?resp=Delete1";
                  }
              });
          }
          else if(operation=="pyg")
          {
              var jqxhr = $.get( '/parametric/clearPyG', function(data) {
                  if(data==false)
                  {
                      window.location.href="/parametric/pyg?resp=UpdateCascade-3";
                  }
                  else
                  {
                      window.location.href="/parametric/pyg?resp=Delete1";
                  }
              });
          }
          else if(operation=="valQueryEeff")
          {
              var jqxhr = $.get( '/ifrs/clearValQueryEeff', function(data) {
                  if(data==false)
                  {
                      window.location.href="/ifrs/valQueryEeff?resp=UpdateCascade-3";
                  }
                  else
                  {
                      window.location.href="/ifrs/valQueryEeff?resp=Delete1";
                  }
              });
          }
          else if(operation=="ciiu")
          {
              var jqxhr = $.get( '/parametric/clearCiiu', function(data) {
                  if(data==false)
                  {
                      window.location.href="/parametric/ciiu?resp=UpdateCascade-3";
                  }
                  else
                  {
                      window.location.href="/parametric/ciiu?resp=Delete1";
                  }
              });
          }
          else if(operation=="type")
          {
              var jqxhr = $.get( '/parametric/clearType', function(data) {
                  if(data==false)
                  {
                      window.location.href="/parametric/typeTemplate?resp=UpdateCascade-3";
                  }
                  else
                  {
                      window.location.href="/parametric/typeTemplate?resp=Delete1";
                  }
              });
          }
      }
      else
            {
              Swal.fire(
                  '¡Operación cancelada!',
                  'Sin acción de respuesta.',
                  'warning'
                );
            }
    })
}
function operationDeleteOne(id,operation)
{
    Swal.fire({
      title: '¿Está seguro?',
      text: '¡Se borrará el elemento con identificador: '+id,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Sí!',
      cancelButtonText: 'No'
    }).then((result) => {
      if (result.isConfirmed)
      {
        if(operation=="third")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
              );
            $.ajax({
              url:'/parametric/removeThirds/'+id,
              type:'GET'}
            );
            setTimeout(function(){
              window.location.href="/parametric/third";
            },2000);
        }
        else if(operation=="signature")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                url:'/parametric/removeSignature/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/parametric/signature";
            },2000);
        }
        else if(operation=="baseicrv")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                url:'/briefcase/removeBaseicrv/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/briefcase/baseicrv";
            },2000);
        }
        else if(operation=="contactosicrv")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                url:'/briefcase/removeContactosicrv/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/briefcase/contactosicrv";
            },2000);
        }
        else if(operation=="priceicrv")
        {
            Swal.fire(
                '¡Base Generada!',
                'Se realizo correctamente la operación..',
                'success'
            );
            $.ajax({
                url:'/briefcase/removePriceicrv/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/briefcase/priceicrv?period="+id;
            },2000);
        }
        else if(operation=="balvaloresicrv")
        {
            Swal.fire(
                '¡Tabla eliminada en el periodo '+id+'!',
                'Se realizo correctamente la operación.',
                'success'
            );
            $.ajax({
                url:'/briefcase/removeBalvaloresicrv/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/briefcase/balvaloresicrv?period="+id;
            },2000);
        }
        else if(operation=="uvricrfall")
        {
            Swal.fire(
                '¡Tabla eliminada en el periodo '+id+'!',
                'Se realizo correctamente la operación.',
                'success'
            );
            $.ajax({
                url:'/briefcase/removeUvricrf/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/briefcase/uvricrf?period="+id;
            },2000);
        }
        else if(operation=="valoresicrv")
        {
            Swal.fire(
                '¡Tabla regenerada en el periodo '+id+'!',
                'Se realizo correctamente la operación.',
                'success'
            );
            $.ajax({
                url:'/briefcase/removeValoresicrv/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/briefcase/valoresicrv?period="+id;
            },2000);
        }
        else if(operation=="fiduciariaicrv")
        {
            Swal.fire(
                '¡Tabla regenerada en el periodo '+id+'!',
                'Se realizo correctamente la operación.',
                'success'
            );
            $.ajax({
                url:'/briefcase/removeFiduciariaicrv/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/briefcase/fiduciariaicrv?period="+id;
            },2000);
        }
        else if(operation=="niveljerarquiaicrv")
        {
            Swal.fire(
                '¡Tabla regenerada en el periodo '+id+'!',
                'Se realizo correctamente la operación.',
                'success'
            );
            $.ajax({
                url:'/briefcase/removeNiveljerarquiaicrv/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/briefcase/niveljerarquiaicrv?period="+id;
            },2000);
        }
        else if(operation=="balfiduciariaicrv")
        {
            Swal.fire(
                '¡Tabla eliminada en el periodo '+id+'!',
                'Se realizo correctamente la operación.',
                'success'
            );
            $.ajax({
                url:'/briefcase/removeBalfiduciariaicrv/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/briefcase/balfiduciariaicrv?period="+id;
            },2000);
        }
        else if(operation=="calculoicrv")
        {
            Swal.fire(
                '¡Base Generada!',
                'Se realizo correctamente la operación..',
                'success'
            );
            $.ajax({
                url:'/briefcase/removeCalculoicrv/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/briefcase/calculoicrv?period="+id;
            },2000);
        }
        else if(operation=="icrv")
        {
            Swal.fire(
                '¡Base Generada!',
                'Se realizo correctamente la operación..',
                'success'
            );
            $.ajax({
                url:'/briefcase/removeIcrv/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/briefcase/icrv?period="+id;
            },2000);
        }
        else if(operation=="f351icrv")
        {
            Swal.fire(
                '¡Base Generada!',
                'Se realizo correctamente la operación..',
                'success'
            );
            $.ajax({
                url:'/briefcase/removeF351icrv/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/briefcase/f351icrv?period="+id;
            },2000);
        }
        else if(operation=="pduicrv")
                {
                    Swal.fire(
                        '¡Base Generada!',
                        'Se realizo correctamente la operación..',
                        'success'
                    );
                    $.ajax({
                        url:'/briefcase/removePduicrv/'+id,
                        type:'GET'}
                    );
                    setTimeout(function(){
                        window.location.href="/briefcase/pduicrv?period="+id;
                    },2000);
                }
        else if(operation=="informe")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                url:'/parametric/removeInforme/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/parametric/informe";
            },2000);
        }
        else if(operation=="informeNotas")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                url:'/parametric/removeInformeNotas/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/parametric/informeNotas";
            },2000);
        }
        else if(operation=="mda")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                url:'/parametric/removeMda/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/parametric/mda";
            },2000);
        }
        else if(operation=="anexo8finrep")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                url:'/reports/removeAnexo8finrep/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/reports/anexo8finrep";
            },2000);
        }
        else if(operation=="accountBanco")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                url:'/parametric/removeAccountBanco/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/parametric/accountBanco";
            },2000);
        }
        else if(operation=="pFechas")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                url:'/parametric/removeFechas/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/parametric/fechas";
            },2000);
        }
        else if(operation=="pNic34")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                url:'/parametric/removeNic34/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/parametric/nic34";
            },2000);
        }
        else if(operation=="pNic34Consol")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                url:'/parametric/removeNic34Consol/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/parametric/nic34Consol";
            },2000);
        }
        else if(operation=="accountCc")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                url:'/accountsReceivable/removeAccountCc/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/accountsReceivable/accountCc";
            },2000);
        }
        else if(operation=="accountCc13")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                url:'/eeffConsolidated/removeAccountParametric/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/eeffConsolidated/parametricsConsolidated";
            },2000);
        }

        else if(operation=="accountCc23")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                url:'/eeffConsolidated/removeAccountParametric1/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/eeffConsolidated/parametricsAjustesMinimos";
            },2000);
        }

        else if(operation=="accountCcMayores1")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                url:'/eeffConsolidated/removeAccountParametricMayores/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/eeffConsolidated/parametricsAjustesMayores";
            },2000);
        }

        else if(operation=="thirdsCc")
        {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                url:'/parametric/removeThirdsCc/'+id,
                type:'GET'}
            );
            setTimeout(function(){
                window.location.href="/parametric/thirdsCc";
            },2000);
        }
        else if(operation=="yntp")
        {
            var jqxhr = $.get( '/parametric/removeYntp/'+id, function(data) {
              if(data==false)
              {
                window.location.href="/parametric/yntp?resp=UpdateCascade-3";
              }
              else
              {
                window.location.href="/parametric/yntp?resp=Delete1";
              }
            });
        }
        else if(operation=="entity")
        {
            var jqxhr = $.get( '/parametric/removeTypeEntity/'+id, function(data) {
                if(data==false)
                {
                    window.location.href="/parametric/typeEntity?resp=UpdateCascade-3";
                }
                else
                {
                    window.location.href="/parametric/typeEntity?resp=Delete1";
                }
            });
        }
        else if(operation=="currency")
        {
            var jqxhr = $.get( '/parametric/removeCurrencies/'+id, function(data) {
              if(data==false)
              {
                window.location.href="/parametric/currency?resp=UpdateCascade-2";
              }
              else
              {
                window.location.href="/parametric/currency?resp=Delete1";
              }
            });
        }
        else if(operation=="responsibleAccount")
        {
            var jqxhr = $.get( '/parametric/removeResponsibleAccount/'+id, function(data) {
              if(data==false)
              {
                window.location.href="/parametric/responsibleAccount?resp=UpdateCascade-1";
              }
              else
              {
                window.location.href="/parametric/responsibleAccount?resp=Delete1";
              }
            });
        }
        else if(operation=="rysParametric")
        {
            var jqxhr = $.get( '/ifrs/removeRysParametric/'+id, function(data) {
              if(data==false)
              {
                window.location.href="/ifrs/rysParametric?resp=UpdateCascade-1";
              }
              else
              {
                window.location.href="/ifrs/rysParametric?resp=Delete1";
              }
            });
        }
        else if(operation=="change")
        {
            var jqxhr = $.get( '/parametric/removeChangeCurrency/'+id, function(data) {
                if(data==false)
                {
                    window.location.href="/parametric/changeCurrency?resp=UpdateCascade-1";
                }
                else
                {
                    window.location.href="/parametric/changeCurrency?resp=Delete1";
                }
            });
        }
        else if(operation=="country")
        {
            var jqxhr = $.get( '/parametric/removeCountry/'+id, function(data) {
              if(data==false)
              {
                window.location.href="/parametric/country?resp=UpdateCascade-4";
              }
              else
              {
                window.location.href="/parametric/country?resp=Delete1";
              }
            });
        }
        else if(operation=="subsidiaries")
        {
            var jqxhr = $.get( '/parametric/removeSubsidiaries/'+id, function(data) {
              if(data==false)
              {
                window.location.href="/parametric/subsidiaries?resp=UpdateCascade-1";
              }
              else
              {
                window.location.href="/parametric/subsidiaries?resp=Delete1";
              }
            });
        }
        else if(operation=="rejectId")
        {
            var jqxhr = $.get( '/ifrs/removeRejectIdP1/'+id, function(data) {
              if(data==false)
              {
                window.location.href="/ifrs/rejectIdP1?resp=UpdateCascade-1";
              }
              else
              {
                window.location.href="/ifrs/rejectIdP1?resp=Delete1";
              }
            });
        }
        else if(operation=="rechazosDescontabilizacionPreCarga")
        {
            var jqxhr = $.get( '/parametric/clearRechazosDescontabilizacion/'+id, function(data) {
              if(data==false)
              {
                window.location.href="/parametric/rechazosDescontabilizacion?resp=UpdateCascade-1";
              }
              else
              {
                window.location.href="/parametric/rechazosDescontabilizacion?resp=Delete1";
              }
            });
        }
        else if(operation=="rejectIdP2")
        {
            var jqxhr = $.get( '/ifrs/removeRejectIdP2/'+id, function(data) {
                if(data==false)
                {
                    window.location.href="/ifrs/rejectIdP2?resp=UpdateCascade-1";
                }
                else
                {
                    window.location.href="/ifrs/rejectIdP2?resp=Delete1";
                }
            });
        }
        else if(operation=="equivalences")
        {
            var jqxhr = $.get( '/ifrs/removeEquivalences/'+id, function(data) {
                if(data==false)
                {
                    window.location.href="/ifrs/equivalences?resp=UpdateCascade-1";
                }
                else
                {
                    window.location.href="/ifrs/equivalences?resp=Delete1";
                }
            });
        }
        else if(operation=="accountControl")
        {
            var jqxhr = $.get( '/ifrs/removeAccountControl/'+id, function(data) {
                if(data==false)
                {
                    window.location.href="/ifrs/accountControl?resp=UpdateCascade-1";
                }
                else
                {
                    window.location.href="/ifrs/accountControl?resp=Delete1";
                }
            });
        }
        else if(operation=="quotas")
         {
                    var jqxhr = $.get( '/ifrs/removeQuota/'+id, function(data) {
                      if(data==false)
                      {
                        window.location.href="/ifrs/quotas?resp=UpdateCascade-1";
                      }
                      else
                      {
                        window.location.href="/ifrs/quotas?resp=Delete1";
                      }
                    });
                }
        else if(operation=="center")
        {
            var jqxhr = $.get( '/ifrs/removeCenter/'+id, function(data) {
                if(data==false)
                {
                    window.location.href="/ifrs/center?resp=UpdateCascade-4";
                }
                else
                {
                    window.location.href="/ifrs/center?resp=Delete1";
                }
            });
        }
        else if(operation=="contract")
        {
            var jqxhr = $.get( '/parametric/removeContract/'+id, function(data) {
              if(data==false)
              {
                window.location.href="/parametric/contract?resp=UpdateCascade-1";
              }
              else
              {
                window.location.href="/parametric/contract?resp=Delete1";
              }
            });
        }
        else if(operation=="indicators")
        {
            var jqxhr = $.get( '/parametric/removeIndicators/'+id, function(data) {
              if(data==false)
              {
                window.location.href="/parametric/indicators?resp=UpdateCascade-1";
              }
              else
              {
                window.location.href="/parametric/indicators?resp=Delete1";
              }
            });
        }
        else if(operation=="reclassification")
        {
            var jqxhr = $.get( '/parametric/removeReclassification/'+id, function(data) {
              if(data==false)
              {
                window.location.href="/parametric/reclassification?resp=UpdateCascade-1";
              }
              else
              {
                window.location.href="/parametric/reclassification?resp=Delete1";
              }
            });
        }
        else if(operation=="provisions")
        {
            var jqxhr = $.get( '/parametric/removeProvisions/'+id, function(data) {
              if(data==false)
              {
                window.location.href="/parametric/provisions?resp=UpdateCascade-1";
              }
              else
              {
                window.location.href="/parametric/provisions?resp=Delete1";
              }
            });
        }
        else if(operation=="neocon")
        {
            var jqxhr = $.get( '/parametric/removeNeocon/'+id, function(data) {
              if(data==false)
              {
                window.location.href="/parametric/neocon?resp=UpdateCascade-1";
              }
              else
              {
                window.location.href="/parametric/neocon?resp=Delete1";
              }
            });
        }
        else if(operation=="rp21")
        {
            var jqxhr = $.get( '/reports/removeRp21/'+id, function(data) {
              if(data==false)
              {
                window.location.href="/reports/rp21?resp=UpdateCascade-1";
              }
              else
              {
                window.location.href="/reports/rp21?resp=Delete1";
              }
            });
        }
        else if(operation=="aval")
        {
            var jqxhr = $.get( '/parametric/removeAval/'+id, function(data) {
                if(data==false)
                {
                    window.location.href="/parametric/avalType?resp=UpdateCascade-1";
                }
                else
                {
                    window.location.href="/parametric/avalType?resp=Delete1";
                }
            });
        }
        else if(operation=="garant")
        {
            var jqxhr = $.get( '/parametric/removeGarantBanks/'+id, function(data) {
                if(data==false)
                {
                    window.location.href="/parametric/garantBank?resp=UpdateCascade-1";
                }
                else
                {
                    window.location.href="/parametric/garantBank?resp=Delete1";
                }
            });
        }
        else if (operation=="Risk")
        {
            var jqxhr = $.get( '/ifrs/removeRisk/'+id, function(data) {
                if(data==false)
                {
                    window.location.href="/ifrs/Risk?resp=UpdateCascade-1";
                }
                else
                {
                    window.location.href="/ifrs/Risk?resp=Delete1";
                }
            });
        }
        else if (operation=="operation")
        {
            var jqxhr = $.get( '/parametric/removeOperation/'+id, function(data) {
                if(data==false)
                {
                    window.location.href="/parametric/operationAccount?resp=UpdateCascade-1";
                }
                else
                {
                    window.location.href="/parametric/operationAccount?resp=Delete1";
                }
            });
        }
        else if (operation=="recon")
        {
            var jqxhr = $.get( '/parametric/removeRec/'+id, function(data) {
                if(data==false)
                {
                    window.location.href="/parametric/reclassificationV2?resp=UpdateCascade-1";
                }
                else
                {
                    window.location.href="/parametric/reclassificationV2?resp=Delete1";
                }
            });
        }
        else if(operation=="templateBank")
        {
            var jqxhr = $.get( '/bank/clearTemplate/'+id, function(data) {
                if(data==false)
                {
                    Swal.fire({
                        position: 'center',
                        icon: 'error',
                        title: '¡Falta Permisos Carga de Documentos!',
                        html: '<p>Su usuario no tiene los permisos para limpiar esta tabla al no encontrarse en un periodo habilitado. Comuníquese con el Consolidador o el Administrador.</p>',
                        showConfirmButton: true,
                        confirmButtonColor: '#004481'
                    })
                }
                else
                {
                    window.location.href="/bank/templateBank?resp=Delete1&period="+id;
                }
            });
        }
        else if (operation == "perimeter") {
            Swal.fire(
                '¡Registros borrados!',
                'Los registros con periodo ' + id + ' han sido borrados correctamente.',
                'success'
            );
            $.ajax({
                    url: '/ifrs/clearPerimeter/' + id,
                    type: 'GET'
                }
            );
            setTimeout(function () {
                window.location.href = "/ifrs/perimeter";
            }, 2000);
        }
        else if (operation == "descontabilizacionReport") {
            Swal.fire(
                '¡Registros borrados!',
                'Los registros con periodo ' + id + ' han sido borrados correctamente.',
                'success'
            );
            $.ajax({
                    url: '/reports/clearDescontabilizacionReport/' + id,
                    type: 'GET'
                }
            );
            setTimeout(function () {
                window.location.href = "/ifrs/perimeter";
            }, 2000);
        }
        else if (operation == "rechazosDescontabilizacion") {
            Swal.fire(
                '¡Registros borrados!',
                'Los registros con periodo ' + id + ' han sido borrados correctamente.',
                'success'
            );
            $.ajax({
                    url: '/parametric/removeRechazosDescontabilizacion/' + id,
                    type: 'GET'
                }
            );
            setTimeout(function () {
                window.location.href = "/parametric/rechazosDescontabilizacion";
            }, 2000);
        }
        else if (operation == "genericAccount") {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                    url: '/parametric/removeGenericAccount/' + id,
                    type: 'GET'
                }
            );
            setTimeout(function () {
                window.location.href = "/parametric/genericAccount";
            }, 2000);
        } else if (operation == "reposYSimultaneas") {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                    url: '/parametric/removeReposYSimultaneas/' + id,
                    type: 'GET'
                }
            );
            setTimeout(function () {
                window.location.href = "/parametric/reposYSimultaneas";
            }, 2000);
        } else if (operation == "counterpartyGenericContracts") {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                    url: '/parametric/removeCounterpartyGenericContracts/' + id,
                    type: 'GET'
                }
            );
            setTimeout(function () {
                window.location.href = "/parametric/counterpartyGenericContracts";
            }, 2000);
        } else if (operation == "accountHistoryIFRS9") {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                    url: '/parametric/removeAccountHistoryIFRS9/' + id,
                    type: 'GET'
                }
            );
            setTimeout(function () {
                window.location.href = "/parametric/accountHistoryIFRS9";
            }, 2000);
        } else if (operation == "accountAndByProduct") {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                    url: '/parametric/removeAccountAndByProduct/' + id,
                    type: 'GET'
                }
            );
            setTimeout(function () {
                window.location.href = "/parametric/accountAndByProduct";
            }, 2000);
        } else if (operation == "provisionsAndProduct") {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                    url: '/parametric/removeProvisionsAndProduct/' + id,
                    type: 'GET'
                }
            );
            setTimeout(function () {
                window.location.href = "/parametric/provisionsAndProduct";
            }, 2000);
        }
        else if (operation == "segmentDecisionTree") {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                    url: '/parametric/removeSegmentDecisionTree/' + id,
                    type: 'GET'
                }
            );
            setTimeout(function () {
                window.location.href = "/parametric/segmentDecisionTree";
            }, 2000);
        } else if (operation == "pyg") {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                    url: '/parametric/removePyG/' + id,
                    type: 'GET'
                }
            );
            setTimeout(function () {
                window.location.href = "/parametric/pyg";
            }, 2000);
        } else if (operation == "type") {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                    url: '/parametric/removeType/' + id,
                    type: 'GET'
                }
            );
            setTimeout(function () {
                window.location.href = "/parametric/typeTemplate";
            }, 2000);
        } else if (operation == "ciiu") {
            Swal.fire(
                '¡Registro borrado!',
                'El registro ha sido borrado correctamente.',
                'success'
            );
            $.ajax({
                    url: '/parametric/removeCiiu/' + id,
                    type: 'GET'
                }
            );
            setTimeout(function () {
                window.location.href = "/parametric/ciiu";
            }, 2000);
        }
      }
      else
      {
        Swal.fire(
            '¡Operación cancelada!',
            'Sin acción de respuesta.',
            'warning'
          );
      }
    });

}

function operationDeletePeriod(periodo,agrupacion,operation)
{
    Swal.fire({
        title: '¿Está seguro?',
        text: '¡Se borrarán los elementos con periodo: '+periodo,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Sí, Borrar!',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            if (operation == "clearConcilifrs9") {
                Swal.fire(
                    '¡Registro borrado!',
                    'Los registros con periodo ' + periodo + ' han sido borrados correctamente.',
                    'success'
                );
                $.ajax({
                        url: '/ifrs/clearConcilifrs9/' + periodo + ":" + agrupacion,
                        type: 'GET'
                    }
                );
                setTimeout(function () {
                    window.location.href = "/ifrs/concilifrs9";
                }, 2000);
            }
        }
        else
        {
            Swal.fire(
                '¡Operación cancelada!',
                'warning'
            );
        }
    });
}

function operationDeleteRec(period,yntp,version)
{
    Swal.fire({
        title: '¿Está seguro?',
        text: 'Al guardar la conciliación para la fecha, se borrarán justificaciones previamente llenadas',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#004481',
        confirmButtonText: 'Sí, Guardar',
        cancelButtonText: 'Volver'
    }).then((result) => {
        if (result.isConfirmed) {
                Swal.fire({
                    position: 'center',
                    icon: 'success',
                    title: 'Conciliación',
                    html: '<p>Se ha guardado la conciliación para la fecha correctamente.</p>',
                    showConfirmButton: true,
                    confirmButtonColor: '#004481'
                });
                $.ajax({
                        url: '/reports/reconciliationIntV1Save?period=' + period + '&yntp=' + yntp + '&version=' + version,
                        type: 'GET'
                    }
                );
                setTimeout(function () {
                    window.location.href = "/reports/reconciliationIntV1Fil?period=";
                }, 2000);
        }
    });
}

function operationDeleteReconIFRS9(type,period)
{
    Swal.fire({
        title: '¿Está seguro?',
        text: '¿Desea eliminar la información para la fecha seleccionada?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#004481',
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Volver'
    }).then((result) => {
        if (result.isConfirmed) {
            if(type == 'EEFF-ALL'){
                Swal.fire({
                    position: 'center',
                    icon: 'success',
                    title: 'Borrado',
                    html: '<p>Se ha borrado la información para la fecha correctamente.</p>',
                    showConfirmButton: true,
                    confirmButtonColor: '#004481'
                });
                $.ajax({
                        url: '/ifrs/clearValQueryEeff?period=' + period,
                        type: 'GET'
                    }
                );
                setTimeout(function () {
                    window.location.href = "/ifrs/valQueryEeff";
                }, 2000);
            }else if(type == 'VAL-IFRS9'){
                Swal.fire({
                    position: 'center',
                    icon: 'success',
                    title: 'Borrado',
                    html: '<p>Se ha borrado la información para la fecha correctamente.</p>',
                    showConfirmButton: true,
                    confirmButtonColor: '#004481'
                });
                $.ajax({
                        url: '/ifrs9/clearValIFRS9?period=' + period,
                        type: 'GET'
                    }
                );
                setTimeout(function () {
                    window.location.href = "/ifrs9/valIFRS9";
                }, 2000);
            }
            else if(type=="interPlane")
            {
                var jqxhr = $.get( '/ifrs9/clearInterPlaneP?period='+period, function(data) {
                    if(data==false)
                    {
                        window.location.href="/ifrs9/plainIFRS9?resp=UpdateCascade-1&period="+period;
                    }
                    else
                    {
                        window.location.href="/ifrs9/plainIFRS9?resp=Delete1&period="+period;
                    }
                });
            }
            else if(type == 'PLAIN-IFRS9'){
                Swal.fire({
                    position: 'center',
                    icon: 'success',
                    title: 'Borrado',
                    html: '<p>Se ha borrado la información para la fecha correctamente.</p>',
                    showConfirmButton: true,
                    confirmButtonColor: '#004481'
                });
                $.ajax({
                        url: '/ifrs9/clearPlainIFRS9?period=' + period,
                        type: 'GET'
                    }
                );
                setTimeout(function () {
                    window.location.href = "/ifrs9/plainIFRS9";
                }, 2000);
            }else if(type == 'MANUAL-ADJ'){
                Swal.fire({
                    position: 'center',
                    icon: 'success',
                    title: 'Borrado',
                    html: '<p>Se ha borrado la información para la fecha correctamente.</p>',
                    showConfirmButton: true,
                    confirmButtonColor: '#004481'
                });
                $.ajax({
                        url: '/ifrs/clearManualAdjustments?period=' + period,
                        type: 'GET'
                    }
                );
                setTimeout(function () {
                    window.location.href = "/ifrs/manualAdjustments";
                }, 2000);
            }else if(type == 'ADJ-HOM'){
                Swal.fire({
                    position: 'center',
                    icon: 'success',
                    title: 'Borrado',
                    html: '<p>Se ha borrado la información para la fecha correctamente.</p>',
                    showConfirmButton: true,
                    confirmButtonColor: '#004481'
                });
                $.ajax({
                        url: '/ifrs/clearAdjustmentsHom?period=' + period,
                        type: 'GET'
                    }
                );
                setTimeout(function () {
                    window.location.href = "/ifrs/adjustmentsHom";
                }, 2000);
            }
            else{
                Swal.fire({
                    position: 'center',
                    icon: 'success',
                    title: 'Borrado',
                    html: '<p>Se ha borrado la información para la fecha correctamente.</p>',
                    showConfirmButton: true,
                    confirmButtonColor: '#004481'
                });
                $.ajax({
                        url: '/ifrs/clearPerimeter?period=' + period,
                        type: 'GET'
                    }
                );
                setTimeout(function () {
                    window.location.href = "/ifrs/perimeter";
                }, 2000);
            }
        }
    });
}

function validateAlerts(operacion,row,column, message = "")
{
    if(operacion=='Modify1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Modificación exitosa!',
            text: 'El registro fue actualizado de forma correcta en el sistema.',
            showConfirmButton: false,
            timer: 4500
        })
    }
    else if(operacion=='Modify0')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Fallo! Modificación fallida',
            text: 'Verifique la información asociada al registro. No se realizaron modificaciones.',
            showConfirmButton: false,
            timer: 4500
        })
    }
    else if(operacion=='JobBaseFiscal-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Fallo! Carga Base Fiscal fallida',
            text: 'Verifique la ruta asociada no encontro los ficheros o no posee permiso a la ruta de cargue (\\\\co.igrupobbva\\svrfilesystem\\TX\\ENVIO_HOST\\XC\\CONSOLIDACION\\) (\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\infocontable\\01-NEXCO\\).',
            showConfirmButton: true
        })
    }
    else if(operacion=='Modify-5')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Fallo! Cuentas Inexistentes en PUC',
            text: 'Verifique las cuentas locales ingresadas.',
            showConfirmButton: false,
            timer: 4500
        })
    }
    else if(operacion=='Concil-01')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Fallo! Data no encontrada',
            text: 'Verifique no hay información cargada.',
            showConfirmButton: false,
            timer: 4500
        })
    }
    else if(operacion=='Concil01')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Conciliación Exitosa',
            text: 'Se ha realizado correctamente el proceso.',
            showConfirmButton: false,
            timer: 4500
        })
    }
    else if(operacion=='General-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Error Operación!',
            text: 'Ha ocurrido un error en la ejecución de la operación.',
            showConfirmButton: false,
            timer: 4500
        })
    }
    else if(operacion=='Add1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Agregado exitosamente!',
            text: 'El registro fue agregado de forma correcta en el sistema.',
            showConfirmButton: false,
            timer: 4500
        })
    }
    else if(operacion=='nat1')
        {
            Swal.fire({
                position: 'center',
                icon: 'success',
                title: '¡Naturaleza Invertida!',
                text: 'Se cambio la naturaleza e la cuenta de forma exitosa.',
                showConfirmButton: false,
                timer: 4500
            })
        }
    else if(operacion=='Add0')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Fallo! Adición fallida',
            text: 'Verifique la información asociada al registro. No se realizo la creación.',
            showConfirmButton: false,
            timer: 4500
        })
    }
    else if(operacion=='AddConCon1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Carga exitosa del documento!',
            text: 'Se realizo el cargue exitoso de la actualización de Contratos.',
            showConfirmButton: false,
            timer: 4500
        });
    }
    else if(operacion=='AddRep1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Carga exitosa del documento!',
            text: 'Se realizo el cargue exitoso de todos los registros contenidos en el documento.',
            showConfirmButton: false,
            timer: 4500
        });
    }
    else if(operacion=='AddRep3')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Confirmación Exitosa!',
            text: 'Se ha confirmado la informacion para este periodo',
            showConfirmButton: false,
            timer: 4500
        });
    }

    else if(operacion=='ProcessExi')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Procesamiento Exitoso!',
            text: 'Se ha procesado las eliminaciones para este periodo',
            showConfirmButton: false,
            timer: 4500
        });
    }

    else if(operacion=='ProcessExi1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Procesamiento Exitos!',
            text: 'Se han obtenido las diferencias por concepto para este periodo',
            timer: 4500
        });
    }

    else if(operacion=='loadAjuste1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Procesar Ajuste Neocon 60!',
            text: 'Se realizo el cargue ajuste exitoso con la base Neocon 60.',
            showConfirmButton: false,
            timer: 4500
        });
    }
    else if(operacion=='AddQuery1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Carga exitoso del Query NIC34!',
            text: 'Se realizo el cargue exitoso de todos los registros contenidos en el documento.',
            showConfirmButton: false
        });
    }
    else if(operacion=='AddQuery-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Carga fallido del Query NIC34!',
            text: 'Este periodo no se encuentra parametrizado para este corte en la tabla de fechas.',
            showConfirmButton: false
        });
    }
    else if(operacion=='Firma1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Carga exitosa de Imagen!',
            text: 'Se realizo el cargue exitoso de la firma.',
            showConfirmButton: false,
            timer: 4500
        });
    }
    else if(operacion=='ExecNIC341')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Ejecución Exitosa Reporte NIC34!',
            text: 'Se realizo la ejecución de todos los registros contenidos en los cortes.',
            showConfirmButton: false,
            timer: 5000
        });
    }
    else if(operacion=='ExecNIC34-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Ejecución Incompleta Reporte NIC34!',
            html: '<p>Inputs incompletos asociados al corte de ejecución</p>\n' +
                '<a class="btn btn-primary text-light mx-1" href="/parametric/fechas">Ver fechas</a>',
            showConfirmButton: false
        });
    }
    else if(operacion=='ExecNIC34-2')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Ejecución Fallida Balance NIC34!',
            html: '<p>No se encuentran los 2 periodos procesados en la base para este corte de ejecución</p>',
            showConfirmButton: false
        });
    }
    else if(operacion=='ExecNIC34-3')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Ejecución Fallida PYG NIC34!',
            html: '<p>No se encuentran los 4 periodos procesados en la base para este corte de ejecución</p>',
            showConfirmButton: false
        });
    }
    else if(operacion=='ExecRules1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Ejecución Exitosa Reglas Data!',
            text: 'Se realizo la ejecución de todos los registros contenidos en la plantilla precargada.',
            showConfirmButton: false,
            timer: 4500
        });
    }
    else if(operacion=='ExecRules2')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Plantilla reglas vacia!',
            text: 'La plantilla de reglas no se encuentra cargada.',
            showConfirmButton: false,
            timer: 4500
        });
    }
    else if(operacion=='errorConAuto')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Falla Proceso!',
            text: 'No hay información cargada para la conciliación.',
            showConfirmButton: false,
            timer: 4500
        });
    }
    else if(operacion=='dates1Porcent-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Fallo Proceso!',
            text: 'No hay información cargada en paramétrica fechas calculo 1%',
            showConfirmButton: false,
            timer: 4500
        });
    }
    else if(operacion=='AddRep0')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Fallo formato documento!',
            text: 'Verifique la información asociada en la posición (Fila: '+row+', Columna: '+column+') no se permiten espacios vacíos ni que excedan su tamaño acordado en la plantilla.',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='AddRepCont-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Fallo formato documento!',
            text: message+'. (Fila: '+row+', Columna: '+column+').',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='AddRep-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Error en documento!',
            text: 'Verifique la información asociada en la posición (Fila: '+row+', Columna: '+column+') no corresponde su formato de tipo de dato.',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }else if(operacion=='AddRep-2')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Error en documento!',
            text: 'Verifique la información asociada en la posición (Fila: '+row+', Columna: '+column+') el código de consolidación no corresponde con la cuenta contable.',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='UpdateCascade-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Dependecia de información!',
            text: 'No puede modificar la PK asociada a otras tablas, elimine los registros de dependencia previamente.',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='error--1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Error al cargar el archivo!',
            text: 'No se pudo cargar el archivo, verifique nuevamente',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='UpdateCascade-2')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Dependecia de información de Divisas!',
            text: 'No puede eliminar, la Divisa está asociada a las tabla Sociedades Yntp, elimine los registros de dependencia previamente.',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='UpdateCascade-3')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Dependecia de Información de Sociedades Yntp!',
            text: 'No puede eliminar, la Sociedad está asociada a la tabla Histórico de Terceros, elimine los registros de dependencia previamente.',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='UpdateCascade-4')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Dependecia de información de Países!',
            text: 'No puede eliminarse, el país está asociado a la tabla Sociedad Yntp, elimine los registros de dependencia previamente.',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='Delete1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Eliminación exitosa!',
            text: 'El registro fue eliminado de forma correcta en el sistema.',
            showConfirmButton: false,
            timer: 4500
        })
    }
    else if(operacion=='porcent')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Cargue Forzado Exitoso!',
            text: 'Se realizo la actualización de forma exitosa.',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='porcent-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Cargue Forzado Fallido!',
            text: 'Ocurrio un error al cargar.',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='RP21-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Carga de Documentos Fallida!',
            html: '<p>Se debe realizar el cargue de los siguientes 5 documentos con su respectivo nombre: <br> FW_CONCILIACION.xlsx <br> RYS_CONCILIACION.xlsx <br> SWAP_CONCILIACION.xlsx <br> OPCIONES_CONCILIACION.xlsx. <br> CRCC_CONCILIACION.xlsx.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='RP21-2')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Falta Permisos Carga de Documentos!',
            html: '<p>Su usuario no posee los permisos para cargar este documento. Comuníquese con el Consolidador o el Administrador.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='PC-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Archivo Inválido!',
            html: '<p>No se realizó el cargue</p><br/><p>El archivo que se intenta cargar se encuentra bloqueado por contraseña (Cargue uno sin cifrado), se encuentra dañado o su versión es muy vieja para procesamiento (Actualice a versión xlsx).</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='ICRF1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Importe Correcto',
            html: '<p>Se ha realizado el cargue de los inputs de forma correcta.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='ICRF-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: 'Ficheros Inexistentes',
            html: '<p>No se encontraron los ficheros solicitados.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='ICRV1-false')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Ejecución Fallida!',
            html: '<p>No se encontro información para procesar</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='ICRV1-true')
        {
            Swal.fire({
                position: 'center',
                icon: 'success',
                title: '¡Generación Exitosa!',
                html: '<p>Se realizó la inserción de la imformación de forma correcta.</p>',
                showConfirmButton: true,
                confirmButtonColor: '#004481'
            })
        }
    else if(operacion=='RP21-4')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Ruta Inaccesible!',
            html: '<p>La ruta a la que intena acceder el sistema no tiene permisos, por favor valide los permisos dados sobre el servidor.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
   else if(operacion=='RP214')
   {
       Swal.fire({
           position: 'center',
           icon: 'success',
           title: '¡Guardado Exitoso!',
           html: '<p>El documento ha sido guardado de forma exitosa en la ruta.</p>',
           showConfirmButton: true,
           confirmButtonColor: '#004481'
       })
   }
    else if(operacion=='CA10')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Documento Cargado!',
            html: '<p>Se ha realizado el cargue de forma exitosa.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='CA11')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Plano Generado!',
            html: '<p>Se ha realizado la creación de forma exitosa.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='CA-11')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Plano Fallido!',
            html: '<p>No se encontraron cuentas para procesar entre los periodos seleccionados.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='CA-12')
        {
            Swal.fire({
                position: 'center',
                icon: 'error',
                title: '¡Plano Fallido!',
                html: '<p>Las fechas inicial no puede ser inferior a la final.</p>',
                showConfirmButton: true,
                confirmButtonColor: '#004481'
            })
        }
    else if(operacion=='AC1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Registro Anulado!',
            html: '<p>Se ha procesado de forma correcta.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='InvCC-2')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡No se seleccionaron registros!',
            html: '<p>Para pagar debe seleccionar previamente los registros a afectar.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='InvCC1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Proceso Completado Exitosamente!',
            html: '<p>Se procesaron correctamente todos los registros pendientes en este periodo</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='InvCC-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡No se encontraron registros!',
            html: '<p>El proceso no encontro registros a procesar en este periodo</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='RP21-3')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Documentos Duplicados!',
            html: '<p>No puede cargar dos documetnos con el mismo nombre.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='RCH-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Carga de Documentos Fallida!',
            html: '<p>Se debe realizar el cargue de los siguientes 6 documentos con su respectivo nombre y extensión en .TXT: </p>' +
                '<div class="table-responsive-sm rounded border-primary">\n' +
                '                <table id="example" class="table table-sm table-striped table-hover text-center table-bordered" width="100%">\n' +
                '                    <tbody>\n' +
                '                    <tr><td> RECHAZOS_CUENTA_PROV_PLAN00 </td></tr>\n' +
                '                    <tr><td> RECHAZOS_CUENTA_RECLASIFICACION_PLAN00 </td></tr>\n' +
                '                    <tr><td> RECHAZOS_CUENTA_IMPUESTOS </td></tr>\n' +
                '                    <tr><td> RECHAZOS_RISTRA_PROV_PLAN00 </td></tr>\n' +
                '                    <tr><td> RECHAZOS_RISTRA_RECLASIFICACION_PLAN00 </td></tr>\n' +
                '                    <tr><td> RECHAZOS_RISTRA_IMPUESTOS </td></tr>\n' +
                '                    </tbody>\n' +
                '                </table>\n' +
                '            </div>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='ReportVertical-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Vertical de Saldos no encontrada!',
            html: '<p>Tabla Vertical inexistente en BD.Comuniquese con el administrador.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='AddRepFiliales')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Carga exitosa del documento!',
            text: 'Se realizo la validación de la fecha y corresponde a los registros contenidos en el documento.',
            showConfirmButton: false,
            timer: 4500
        });
    }
    else if(operacion=='AddRepFilialesFallido')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Falla Proceso!',
            text: 'Se realizo la validación de la fecha y NO corresponde a los registros contenidos en el documento.',
            showConfirmButton: false,
            timer: 4500
        });
    }
    else if(operacion=='Correo Exitoso')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Informacion Confirmada',
            text: 'Se ha confirmado el cargue de los tres archivos de la filial',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='rol-pucBanco')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡El periodo no corresponde a la fecha!',
            html: '<p>No se realizó el cargue</p><br/><p>No se Cargo el archivo debido a que no corresponde al periodo seleccionado</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='rol-pucBancoBien')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Archivo Cargado Exitosamente!',
            html: '<p>¡Carga exitosa de información!</p><br/><p>Se ha realizado la carga exitosa del archivo Query de Banco</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='ReportVertical')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Vertical de Saldos Cargada!',
            html: '<p>Tabla Vertical cargada correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='LoadDoc-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Documento no cargado!',
            html: '<p>Extensión de documento no soportada. Actualice la extensión del documento cargado .xls a .xlsx.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='LoadDocPage-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Error Documento!',
            html: '<p>Página del documento no soportada. No se encuentra la segunda hoja del libro de Excel.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='ModifyCurrency-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Divisa invaida!',
            html: '<p>La divisa ingresada no es valida.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='QueryRP21-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Validación Estados',
            html: '<p>Los estados de los Inputs RP21 deben estar en Pendiente (Icono Adertencia Amarillo).</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='QueryRP21')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Validación Exitosa',
            html: '<p>Los Saldos coinciden correctamente.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='QueryRP21-2')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Fallo Query',
            html: '<p>El Query no se encuentra cargado en el aplicativo.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='MIS1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'MIS Actualizado',
            html: '<p>Se ha actualizado la información correctamente.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='MIS-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Fallo MIS',
            html: '<p>El MIS no se encuentra cargado en el aplicativo para este periodo.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }else if(operacion=='TX-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Cargue Base Fiscal',
            html: '<p>Fichero cargado correctamente.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'

        })
    }else if(operacion=='TX-2')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Fallo Base Fiscal',
            html: '<p>El formato del fichero no es el correcto.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }else if(operacion=='formatAdj')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Fallo Carga Ajustes',
            html: '<p>El formato del fichero no es el correcto, debe ser .xlsx.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='TX-3')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Fallo Base Fiscal',
            html: '<p>Se presentó error en la carga del fichero.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='responsibleAccount-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Inserción Fallida!',
            html: '<p>El Centro de Costos asociado a la cuenta no es valido. (Se espera un valor númerico de 4 digitos)</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }else if(operacion=='IRP-0')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: 'Confirmación RP21 - Intergrupo',
            html: '<p>Se ha confirmado la información sin tener cuadre con la contabilidad. Ya se encuentra disponible en formato Intergrupo.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }else if(operacion=='IRP-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Fallo Confirmación RP21 - Intergrupo',
            html: '<p>Se presentó error en la inserción de la información.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }else if(operacion=='IRP-2')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Confirmación RP21 - Intergrupo',
            html: '<p>Se ha confirmado la información. Ya se encuentra disponible en formato Intergrupo.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }else if(operacion=='IRP-3')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Fallo Confirmación RP21 - Intergrupo',
            html: '<p>No existe información para la fecha seleccionada.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }else if(operacion=='DRP-0')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Borrado Input',
            html: '<p>Información eliminada correctamente.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }else if(operacion=='DRP-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Borrado Input',
            html: '<p>Se presentó error al eliminar la información.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='ModifyCenter')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Actualización Exitosa',
            html: '<p>Proceda a realizar el ajuste del centro '+row+' en paramétrica de usuarios ya que no tiene responsables asigandos.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='updateS')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Actualización Exitosa',
            html: '<p>Se realizó el cargue de la información proveniente de SICC.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='updateSF')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Fallo Actualización SICC',
            html: '<p>Fallo de cargue datos de documento SICC.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='sendBankTemplateCorrect')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Enviado a Intergrupo V1',
            html: '<p>Se ha enviado correctamente a Intergrupo V1</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='sendBankTemplateFail')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Fallo en envio a Intergrupo',
            html: '<p>Revise que la información sea correcta e intente nuevamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='confirmDataRG')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Input Confirmados Correctamente',
            html: '<p>Se ha conifrmado la data cargada. Recuérde realizar la limpieza de las tablas de creación de cuentas.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='confirmDataR')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Input Confirmados Correctamente',
            html: '<p>Se ha conifrmado la data cargada correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='confirmDataR-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Fallo al confirmar Inputs',
            html: '<p>Revise que la información cargada sea correcta e intente nuevamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='confirmDataC-GENERAL')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Información Incompleta',
            html: '<p>Revise que la información obligatoria este diligenciada.</p><p>(EMPRESA,NÚMERO CUENTA,NOMBRE CUENTA,NOMBRE CORTO CUENTA,TIPOCTA,INDIC L/I,MON)</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='confirmDataC-GESTION')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Información Incompleta',
            html: '<p>Revise que la información cargada no puede confrimar información vacía</p><p>(CÓDIGO GESTIÓN,EPIGRAFE)</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='confirmDataC-CONSOLIDACION')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Información Incompleta',
            html: '<p>Revise que la información cargada no puede confrimar información vacía</p><p>(CONSOLID)</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='confirmDataC-CONTROL CONTABLE')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Información Incompleta',
            html: '<p>Revise que la información cargada no puede confrimar información vacía</p<p>(CÓDIGO DE CONTROL,DIAS DE PLAZO,INDICADOR DE LA CUENTA,TIPO DE APUNTE,INVENTARIABLE)</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='CONTCM')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Error Carga Masiva',
            html: '<p>Se debe seleccionar al menos una opción</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ContingentesLoadFail')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error Carga Contingentes',
            html: '<p>Verifique que el formato del archivo es .xls ,.xlsx o que el archivo no tiene contraseña</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='rysConcilLoadFail')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error Carga Cruce Conciliación',
            html: '<p>Verifique la información de cargue.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ContingentesLoadFailDivisa')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error Divisas',
            html: '<p>Las divisas para la fecha no se encuentran cargadas</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='sendSubTemplateCorrect')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Enviado a Intergrupo Filiales',
            html: '<p>Se ha cargado correctamente la informacion para Intergrupo Filiales</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='sendSubTemplateFail')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Fallo en el envío a Intergrupo Filiales',
            html: '<p>Verifique que la información no esté vacía</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='AddRepFiliales')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Carga exitosa del documento!',
            text: 'Se realizo la validación de la fecha y corresponde a los registros contenidos en el documento.',
            showConfirmButton: false,
            timer: 4500
        });
    }

    else if(operacion=='AddRepFilialesFallido')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Falla Proceso!',
            text: 'Se realizo la validación de la fecha y NO corresponde a los registros contenidos en el documento.',
            showConfirmButton: false,
            timer: 4500
        });
    }
    else if(operacion==='deleteBaseFiscal')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Elminación Correcta',
            html: '<p>Se han borrado los registros de base fiscal para el periodo</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='sendInterCont')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Confirmación Contingentes - Intergrupo',
            html: '<p>Se ha confirmado la información. Ya se encuentra disponible en formato Intergrupo.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='contAdd')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Inserción Correcta Contingentes',
            html: '<p>Se ha confirmado la carga de la información de forma exitosa.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ErrorCuentaTamanio')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error en la cuenta introducida',
            html: '<p>La cuenta debe tener 5 caracteres</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ErrorCuentaTamanio9')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error en la cuenta introducida',
            html: '<p>La cuenta debe tener entre 4 y 18 caracteres</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='Error1CuentaTamanio9')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error en la cuenta introducida',
            html: '<p>La cuenta debe tener entre 9 y 18 caracteres</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ErrorCuentaCaracteres')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error en la cuenta introducida',
            html: '<p>La cuenta debe tener solo números</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ErrorCodigoTamanio')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error en el código introducido',
            html: '<p>El código debe tener 3 caracteres</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ErrorCodigoTamanio5')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error en el código introducido',
            html: '<p>El código debe tener 5 caracteres</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ErrorCodigoCaracteres')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error en el código introducido',
            html: '<p>El código debe tener solo números</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ErrorContratoTamanio18')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error en el contrato introducido',
            html: '<p>El contrato debe tener 18 caracteres</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='Error1ContratoTamanio20')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error en el contrato introducido',
            html: '<p>El contrato origen debe tener de 17 a 20 caracteres</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='Error2ContratoTamanio20')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error en el contrato introducido',
            html: '<p>El contrato ifrs9 debe tener de 4 a 20 caracteres</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ErrorContratoCaracteres')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error en el contrato introducido',
            html: '<p>El contrato debe tener solo números</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ErrorSaldo')
        {
            Swal.fire({
                position: 'center',
                icon: 'error',
                title: 'Error en el saldo introducito',
                html: '<p>El saldo debe tener solo números</p>',
                showConfirmButton: true,
                confirmButtonColor: '#004481'
            })
        }
    else if(operacion==='ErrorSigno')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error en el signo introducido',
            html: '<p>El signo solo puede ser "+" o "-"</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ErrorDivisa')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error con la divisa introducida',
            html: '<p>La divisa debe estar formada por 3 caracteres</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ErrorCuentaPyGCaracteres')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Error con la cuenta de PyG introducida',
            html: '<p>La cuenta PyG debe contener solo números</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='validacionFallida')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: 'Validación Query contra EEFF Realizada',
            html: '<p>Hay diferencias entre Query y EEFF</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='validacionCorrecta')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Validación Query contra EEFF Realizada',
            html: '<p>Query y EEFF cuadran correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ejecucionCorrecta')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Validación IFRS9',
            html: '<p>Proceso Ejecutado Correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ejecucionCorrectaPlanos')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Plantillas IFRS9',
            html: '<p>Proceso para Generación de Plantillas Ejecutado Correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ejecucionCorrectaPlanos-1')
    {
        Swal.fire({
            title: 'Plantillas IFRS9 ',
            text: 'Proceso para Generación de Plantillas tiene las siguientes cuentas que no puede reportar saldo Intergrupo',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#004481',
            cancelButtonColor: '#004481',
            confirmButtonText: 'Descargar Listado',
            cancelButtonText: 'Omitir'
        }).then((result) => {
            if (result.isConfirmed) {
                Swal.fire({
                    position: 'center',
                    icon: 'success',
                    title: 'Plantillas IFRS9',
                    html: '<p>Se ha descargado correctamente el listado.</p>',
                    showConfirmButton: true,
                    confirmButtonColor: '#004481'
                });
                setTimeout(function () {
                    window.location.href = "/ifrs9/plainIFRS9/listNeoconValidation?period=" + row;
                }, 1000);
            }
        });
    }
    else if(operacion==='ejecucionCorrectaPlanosPYG')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Confirmación PYG',
            html: '<p>Proceso para confirmación data de PYG ejecutado correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ejecucionCorrectaPlanosPYG2')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Confirmación REPOS',
            html: '<p>Proceso para confirmación data de REPOS ejecutado correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ejecucionCorrectaPlanosImpu')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Confirmación Impuestos',
            html: '<p>Proceso para confirmación data de Impuestos ejecutado correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ejecucionCorrectaPlanosPYG-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: 'Exclusión PYG',
            html: '<p>Proceso para excluir data de PYG ejecutado correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ejecucionCorrectaPlanosPYG2-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: 'Exclusión REPOS',
            html: '<p>Proceso para excluir data de REPOS ejecutado correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='ejecucionCorrectaPlanosImpu-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: 'Exclusión Impuestos',
            html: '<p>Proceso para excluir data de Impuestos ejecutado correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='desconParcialRealizado')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Descontabilización parcial',
            html: '<p>Descontabilización parcial realizada correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='desconFinalRealizado')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Descontabilización completa',
            html: '<p>Descontabilización realizada correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='revisarConcil')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Descontabilización no realizada',
            html: '<p>La conciliación aun no cuadra, no se puede descontabilizar</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='RechazosNoEncontrado')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Rechazos no encontrado',
            html: '<p>Fichero rechazos (RECHAZOS_DESCON_PROV_PLAN00.TXT) no encontrado. Si la ruta se registra manualmente recuerde conservar la estructura (Ejemplo: \\co.igrupobbva\svrfilesystem\TX\ENVIO_HOST\XC\CONSOLIDACION\)</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='granConsoFallida')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: 'Proceso Gran Consolidación Realizado',
            html: '<p>Hay cuentas que deben ser revisadas</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='granConsoCorrecta')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Proceso Gran Consolidación Realizado',
            html: '<p>Todas las cuentas están correctas</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='cuadreFallido')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: 'Proceso Cuadre Query EEFF Realizado',
            html: '<p>Hay cuentas que deben ser revisadas</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='cuadreCorrecto')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Proceso Cuadre Query EEFF Realizado',
            html: '<p>Todas las cuentas están correctas</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='AnexosTerminado')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Anexos 8 Cupos',
            html: '<p>Proceso de Anexos 8 Cupos finalizado correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion==='AnexosFaltaParametria')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: 'Anexos 8 Cupos',
            html: '<p>Falta cargar la parametría</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='JustSave-0')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Conciliación',
            html: '<p>Error al guardar la justificación.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='JustSave-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Conciliación',
            html: '<p>Justificación guardada correctamente.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='ReconSave')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Conciliación',
            html: '<p>Se ha guardado la conciliación para la fecha correctamente.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion == 'subsidiariesCorrect'){
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Filiales Cargadas Correctamente',
            html: '<p>Se ha cargado correctamente la plantilla de filiales</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion == 'segmentsCorrect'){
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Segmentos Actualizados Correctamente',
            html: '<p>Se han cargado correctamente la actualización de Segmentos</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='segments-error')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Inserción Segmentos',
            html: '<p>El documento cuenta con números de cliente duplicados.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion == 'ApuntesCorrect'){
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Cargue Apuntes Riesgos',
            html: '<p>Apuntes Riesgos cargados correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion == 'manualsCorrect'){
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Cargue Ajustes Manuales',
            html: '<p>Ajustes Manuales cargados correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion == 'adjCorrect'){
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Cargue Ajustes Homogeneización',
            html: '<p>Ajustes Homogeneización cargados correctamente</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='contAdd')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Inserción Correcta Contingentes',
            html: '<p>Se ha confirmado la carga de la información de forma exitosa.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='rysConcilAdd')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Generación Correcta Conciliación',
            html: '<p>Se ha confirmado el cruce de la información de forma exitosa.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='AddRecla-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Inserción Reclasificaciones',
            html: '<p>El código de consolidación insertado no coincide con la cuenta contable.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='Reclas-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Reclasificación Cartera',
            html: '<p>Proceso Reclasificación Cartera realizado con éxito.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='Reclas-2')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Envío Intergrupo V2',
            html: '<p>Se ha insertado en Intergrupo V2 correctamente.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='inter-3')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Envío Intergrupo V3',
            html: '<p>Se ha insertado en Intergrupo V3 correctamente.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='RECONDIFF-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Cruce Intergrupo - Query',
            html: '<p>Se ha cruzado la información sin diferencias.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='ANX1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Cargue exitoso ANEXO 8',
            html: '<p>Se ha realizado el cruce exitoso.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='ANX-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Fallo Cargue ANEXO 8',
            html: '<p>No sé ha encontrado información.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='ANXM1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: 'Cargue exitoso Manuales (ANEXO 8 - SICC)',
            html: '<p>Se ha realizado el cruce exitoso.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='ANXM-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: 'Fallo Cargue Manuales (ANEXO 8 - SICC)',
            html: '<p>No sé ha encontrado información.</p>',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='Generic')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Fallo formato documento!',
            text: 'Verifique la información asociada en la posición (Fila: '+row+', Columna: '+column+') no se permiten espacios vacíos ni que excedan su tamaño acordado en la plantilla.',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }else if(operacion=='ErrorIntV2-0')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Fallo actualización!',
            text: 'El YNTP ingresado no se encuentra en la parametría de Sociedades YNTP',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='ErrorIntV2-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Fallo actualización!',
            text: 'La Cuenta Local ingresada no se encuentra en el Plan de Cuentas',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='ErrorIntV2-2')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Fallo actualización!',
            text: 'La Cuenta Neocon ingresada no se encuentra en el Plan de Cuentas',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='updateDataC')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Actualización Completa!',
            text: 'Se actualizaron los registros correctamente',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='updateDataC-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Fallo Actualización!',
            text: 'No fue posible actualizar los registros',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='stageDataC')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Creación Completa!',
            text: 'Se crearon los registros de stage por rechazos',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='stageDataC-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Fallo Creación!',
            text: 'No fue posible crear los registros de stages',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='correctPlainInter')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Carga exitosa de información!',
            text: 'Archivos Intergrupo Cargados Correctamente',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='errorCurrPlainInter')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Error Proceso!',
            text: 'No hay divisas cargadas para la fecha.',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='errorDatePlainInter')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Error Proceso!',
            text: 'La fecha de proceso no coincide con la fecha contable de los archivos o el nombre de los archivos no son aceptados.',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='loadFail-2')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Autorización Invalida de cargue!',
            text: 'No fue posible cargar el documento ya que no se encuentra en proceso de validación',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='loadPR1')
    {
        Swal.fire({
            position: 'center',
            icon: 'success',
            title: '¡Carga exitosa de información!',
            text: 'Se cargó la información correctamente.',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='loadPR-1')
    {
        Swal.fire({
            position: 'center',
            icon: 'warning',
            title: '¡Información Vacía!',
            text: 'No se encontro información para cargar.',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='loadPR-2')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Fallo al realizar el cargue!',
            text: 'No fue posible cargar la infromación',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='Descon-2')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Fallo al realizar el cargue!',
            text: 'Se excede la cantidad de registros de excel, por favor revise no tener celdas con formato en blanco al final del documento.',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }
    else if(operacion=='Descon-3')
    {
        Swal.fire({
            position: 'center',
            icon: 'error',
            title: '¡Fallo al realizar el cargue!',
            text: 'No es posible cargar un documento con contraseña, por favor retirarla y volver a  cargar.',
            showConfirmButton: true,
            confirmButtonColor: '#004481'
        })
    }

}

let createPersonalAlert = (state,title,content,url,period) => {
    Swal.fire({
      position: 'center',
      icon: state,
      title: title,
      html: content,
      showConfirmButton:true,
        confirmButtonColor: '#004481',
    }).then(result => {
            if(result.isConfirmed){
                if(period === undefined){
                    window.location.href = url
                }else{
                    window.location.href = url+'?period='+period
                }

            }
        }
    )
}

let createCustomConfirmAlert = (period,version,title,content,url) => {
    Swal.fire({
        title: title,
        text: content,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Confirmar'
    }).then((result) => {
        if (result.isConfirmed) {

            window.location.href = '/ifrs9/onePercent/forceData/upload'+'?period='+period+'&version='+version

            Swal.fire({
                title: 'Version cargada correctamente',
                text: `<p> Se ha cargado la version ${version} para el 1% en el periodo ${period} </p>`,
                showConfirmButton: true,
                confirmButtonColor: '#004481'
            }).then(result =>{
                if(result.isConfirmed){
                    window.location.href = url+'?period='+period
                }
            })
        }
    })
}