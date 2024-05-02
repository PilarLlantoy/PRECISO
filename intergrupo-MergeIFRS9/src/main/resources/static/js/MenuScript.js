// Menu inicial

const inter = document.getElementById('btnIntergrupo')
const ifrs9 = document.getElementById('btnIfrs9')
const creacion = document.getElementById('btnCreacion')
const provisiones = document.getElementById('btnProvisiones')
const informacion = document.getElementById('btnInformacion')
const portafolio = document.getElementById('btnPortafolio')
const parametria = document.getElementById('btnParametria')
const admin = document.getElementById('btnAdministracion')
const dataquality = document.getElementById('btnDataQuality')
const eeffMenu = document.getElementById('btneeffMenu')
const collectionAccount = document.getElementById('btnCollectionAccount')
const nic34 = document.getElementById('btnReportNIC34')
const overlay = document.getElementById('overlay')

overlay.addEventListener('click', () => {
    overlay.classList.add('no_overlay')
    removeShowMenu()
    removeShowSubmenu()
})

inter.addEventListener('click', () =>{
    removeShowMenu()
    removeShowSubmenu()
    let menu = document.getElementById('intergrupo_menu')

    if(menu.classList.contains('show_menu')){
        menu.classList.remove('show_menu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_menu')
        overlay.classList.remove('no_overlay')
    }
})

ifrs9.addEventListener('click', ()=>{
    removeShowMenu()
    removeShowSubmenu()
    let menu = document.getElementById('ifrs9_menu')

    if(menu.classList.contains('show_menu')){
        menu.classList.remove('show_menu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_menu')
        overlay.classList.remove('no_overlay')
    }
})

creacion.addEventListener('click', ()=>{
    removeShowMenu()
    removeShowSubmenu()
    let menu = document.getElementById('creacion_menu')

    if(menu.classList.contains('show_menu')){
        menu.classList.remove('show_menu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_menu')
        overlay.classList.remove('no_overlay')
    }
})

provisiones.addEventListener('click', ()=>{
    removeShowMenu()
    removeShowSubmenu()
    let menu = document.getElementById('provisiones_menu')

    if(menu.classList.contains('show_menu')){
        menu.classList.remove('show_menu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_menu')
        overlay.classList.remove('no_overlay')
    }
})

informacion.addEventListener('click', ()=>{
    removeShowMenu()
    removeShowSubmenu()
    let menu = document.getElementById('informacion_menu')

    if(menu.classList.contains('show_menu')){
        menu.classList.remove('show_menu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_menu')
        overlay.classList.remove('no_overlay')
    }
})

portafolio.addEventListener('click', ()=>{
    removeShowMenu()
    removeShowSubmenu()
    let menu = document.getElementById('portafolio_menu')

    if(menu.classList.contains('show_menu')){
        menu.classList.remove('show_menu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_menu')
        overlay.classList.remove('no_overlay')
    }
})

parametria.addEventListener('click', ()=>{
    removeShowMenu()
    removeShowSubmenu()
    let menu = document.getElementById('parametria_menu')

    if(menu.classList.contains('show_menu')){
        menu.classList.remove('show_menu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_menu')
        overlay.classList.remove('no_overlay')
    }
})

admin.addEventListener('click', ()=>{
    removeShowMenu()
    removeShowSubmenu()
    let menu = document.getElementById('admin_menu')

    if(menu.classList.contains('show_menu')){
        menu.classList.remove('show_menu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_menu')
        overlay.classList.remove('no_overlay')
    }
})

eeffMenu.addEventListener('click', ()=> {
    removeShowMenu()
    removeShowSubmenu()
    let menu = document.getElementById('eeffMenu')

    if(menu.classList.contains('show_menu')){
        menu.classList.remove('show_menu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_menu')
        overlay.classList.remove('no_overlay')
    }
})

dataquality.addEventListener('click', ()=>{
    removeShowMenu()
    removeShowSubmenu()
    let menu = document.getElementById('dq_menu')

    if(menu.classList.contains('show_menu')){
        menu.classList.remove('show_menu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_menu')
        overlay.classList.remove('no_overlay')
    }
})
collectionAccount.addEventListener('click', ()=>{
    removeShowMenu()
    removeShowSubmenu()
    let menu = document.getElementById('cuentasCobro_menu')

    if(menu.classList.contains('show_menu')){
        menu.classList.remove('show_menu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_menu')
        overlay.classList.remove('no_overlay')
    }
})

nic34.addEventListener('click', ()=>{
    removeShowMenu()
    removeShowSubmenu()
    let menu = document.getElementById('nic34_menu')

    if(menu.classList.contains('show_menu')){
        menu.classList.remove('show_menu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_menu')
        overlay.classList.remove('no_overlay')
    }
})

let removeShowMenu = () =>{
    var elements = document.getElementsByClassName('show_menu');

    while(elements.length > 0){
        elements[0].classList.remove('show_menu');
    }
}

let removeShowSubmenu = () =>{
    var sub = document.getElementsByClassName('show_submenu');

    while(sub.length > 0){
        sub[0].classList.remove('show_submenu');
    }
}

// Submenu

//Cuentas Por Cobrar

const paramCC = document.getElementById('btnParamCuC')

paramCC.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('parametrica_cuc')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

// Intergrupo
const pci = document.getElementById('btnPlantillaCarga')
const comer = document.getElementById('btnComercializadora')
const conciliacion = document.getElementById('btnConciliacion')
const neoconInter = document.getElementById('btnPlantillaNeocon')
const paramInter = document.getElementById('btnParametricasInter')
const contingentes = document.getElementById('btnContingentes')

pci.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('plantilla_carga')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

contingentes.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('contingentes')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

comer.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('depositos')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

conciliacion.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('conciliacion')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

neoconInter.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('neoconInter')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

paramInter.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('paramInter')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

//IFRS9
const general = document.getElementById('btnCuadreGeneral')
const segmentos = document.getElementById('btnSegmentos')
const paramIfrs9 = document.getElementById('btnParamIfrs9')
const entradasIFRS9 = document.getElementById('btnEntradasIFRS9')
const riskIFRS9 = document.getElementById('btnRiskIFRS9')
const valDesc = document.getElementById('btnValDesc')
const neoconMenu = document.getElementById('btnNeocon')

general.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('cuadreGeneral')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

segmentos.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('segmentos')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

entradasIFRS9.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('entradasIFRS9')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

valDesc.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('valDesc')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

neoconMenu.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('neoconMenu')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

paramIfrs9.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('paramIFRS9')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

riskIFRS9.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('riskIFRS9')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

//Provisiones

const paramProv = document.getElementById('btnParamProv')

paramProv.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('paramProv')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

//Informacion

const local = document.getElementById('btnInfoLocal')
const neocon = document.getElementById('btnInfoNeocon')

local.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('infoLocal')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

neocon.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('infoNeocon')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

//PORTAFOLIO

const icrv = document.getElementById('btnICRV')
const icrf = document.getElementById('btnICRF')
const portaParam = document.getElementById('btnPortaParam')

icrv.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('infoICRV')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

icrf.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('infoICRF')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

portaParam.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('infoPortaParam')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

//PARAMETRICAS

const paramFuncionales = document.getElementById('btnPrFunc')
const paramContables = document.getElementById('btnPrCont')
const paramGenerales = document.getElementById('btnParamGen')

paramFuncionales.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('infoPrFunc')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

paramContables.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('infoPrCont')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

paramGenerales.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('infoParamGen')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

//Creacion de cuentas

const paramCc = document.getElementById('btnParamCc')

paramCc.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('paramCc')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

const plan00 = document.getElementById('btnPlan00')

plan00.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('plan00')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

const otrasCuentas = document.getElementById('btnOtrasCuentas')

otrasCuentas.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('otrasCuentas')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

//EEFF Consolidado

const filialesF = document.getElementById('btnPlantillafiliales')

filialesF.addEventListener('click', ()=> {
    let menu = document.getElementById('plantilla_filiales')

    removeShowSubmenu()
    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

const filiales3 = document.getElementById('btnFiliales_3')

filiales3.addEventListener('click', ()=> {
    let menu = document.getElementById('filiales_3')

    removeShowSubmenu()
    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

const filialesEliminaciones = document.getElementById('btnEliminacionesFiliales')

filialesEliminaciones.addEventListener('click', ()=> {
    let menu = document.getElementById('Eliminaciones_filiales')
    removeShowSubmenu()
    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

//NIC34

const paramNIC34 = document.getElementById('btnParamNIC34')

paramNIC34.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('paramNIC34')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

const eeffNIC34 = document.getElementById('btnEEFFNIC34')

eeffNIC34.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('NIC34EEFFSeparado')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

const eeffNIC34Consol = document.getElementById('btnEEFFNIC34Consol')

eeffNIC34Consol.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('NIC34EEFFConsol')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

const NIC34Separado = document.getElementById('btnNIC34Separado')

NIC34Separado.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('NIC34Separado')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})

const NIC34Consol = document.getElementById('btnNIC34Consol')

NIC34Consol.addEventListener('click', ()=> {
    removeShowSubmenu()
    let menu = document.getElementById('NIC34Consol')

    if(menu.classList.contains('show_submenu')){
        menu.classList.remove('show_submenu')
        overlay.classList.add('no_overlay')
    } else{
        menu.classList.add('show_submenu')
        overlay.classList.remove('no_overlay')
    }
})