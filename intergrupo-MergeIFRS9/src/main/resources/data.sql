-- Add CARGOS
IF NOT EXISTS(SELECT * FROM preciso_administracion_cargos WHERE id_cargo = 1) BEGIN INSERT INTO preciso_administracion_cargos (nombre_cargo) VALUES ('Gerente') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_cargos WHERE id_cargo = 2) BEGIN INSERT INTO preciso_administracion_cargos (nombre_cargo) VALUES ('Profesional Especializado') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_cargos WHERE id_cargo = 3) BEGIN INSERT INTO preciso_administracion_cargos (nombre_cargo) VALUES ('Profesional') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_cargos WHERE id_cargo = 4) BEGIN INSERT INTO preciso_administracion_cargos (nombre_cargo) VALUES ('Analista') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_cargos WHERE id_cargo = 5) BEGIN INSERT INTO preciso_administracion_cargos (nombre_cargo) VALUES ('Ingeniero') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_cargos WHERE id_cargo = 6) BEGIN INSERT INTO preciso_administracion_cargos (nombre_cargo) VALUES ('Consultor') END

-- Add PAISES
IF NOT EXISTS(SELECT * FROM preciso_paises WHERE id_pais = 1) BEGIN INSERT INTO preciso_paises (nombre_pais, sigla_pais) VALUES ('Colombia', 'CO') END
IF NOT EXISTS(SELECT * FROM preciso_paises WHERE id_pais = 2) BEGIN INSERT INTO preciso_paises (nombre_pais, sigla_pais) VALUES ('España', 'ES') END
IF NOT EXISTS(SELECT * FROM preciso_paises WHERE id_pais = 3) BEGIN INSERT INTO preciso_paises (nombre_pais, sigla_pais) VALUES ('Estados Unidos', 'US') END

-- Add DIVISAS
IF NOT EXISTS(SELECT * FROM preciso_divisas WHERE id_divisa = 1) BEGIN INSERT INTO preciso_divisas (nombre_divisa, sigla_divisa) VALUES ('Peso Colombiano', 'COP') END
IF NOT EXISTS(SELECT * FROM preciso_divisas WHERE id_divisa = 2) BEGIN INSERT INTO preciso_divisas (nombre_divisa, sigla_divisa) VALUES ('Euro', 'EUR') END
IF NOT EXISTS(SELECT * FROM preciso_divisas WHERE id_divisa = 3) BEGIN INSERT INTO preciso_divisas (nombre_divisa, sigla_divisa) VALUES ('Dolar', 'USD') END

-- Add SISTEMAS FUENTE
IF NOT EXISTS(SELECT * FROM preciso_sistema_fuente WHERE id_sf = 1) BEGIN INSERT INTO preciso_sistema_fuente (nombre_sf, sigla_sf, festivo, id_pais, activo) VALUES ('Dialogo', 'DIA', 1, 1, 1) END
IF NOT EXISTS(SELECT * FROM preciso_sistema_fuente WHERE id_sf = 2) BEGIN INSERT INTO preciso_sistema_fuente (nombre_sf, sigla_sf, festivo, id_pais, activo) VALUES ('Bank Trade', 'EY0', 0, 1, 0) END
IF NOT EXISTS(SELECT * FROM preciso_sistema_fuente WHERE id_sf = 3) BEGIN INSERT INTO preciso_sistema_fuente (nombre_sf, sigla_sf, festivo, id_pais, activo) VALUES ('Extranjera', 'GA0', 1, 2, 1) END


-- Add TIPOS EVENTOS
IF NOT EXISTS(SELECT * FROM preciso_tipo_evento WHERE id_tipo_evento = 1) BEGIN INSERT INTO preciso_tipo_evento (nombre_tipo_evento) VALUES ('Conciliación') END
IF NOT EXISTS(SELECT * FROM preciso_tipo_evento WHERE id_tipo_evento = 2) BEGIN INSERT INTO preciso_tipo_evento (nombre_tipo_evento) VALUES ('Notas') END
IF NOT EXISTS(SELECT * FROM preciso_tipo_evento WHERE id_tipo_evento = 3) BEGIN INSERT INTO preciso_tipo_evento (nombre_tipo_evento) VALUES ('Resultados') END

-- Add TIPO DOCUMENTO
IF NOT EXISTS(SELECT * FROM preciso_administracion_tipo_documento WHERE id_tipo_documento = 1) BEGIN INSERT INTO preciso_administracion_tipo_documento (nombre_tipo_documento,codigo_tipo_documento) VALUES ('ACCIONISTA','Z') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_tipo_documento WHERE id_tipo_documento = 2) BEGIN INSERT INTO preciso_administracion_tipo_documento (nombre_tipo_documento,codigo_tipo_documento) VALUES ('CARNET DIPLOMATICO','A') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_tipo_documento WHERE id_tipo_documento = 3) BEGIN INSERT INTO preciso_administracion_tipo_documento (nombre_tipo_documento,codigo_tipo_documento) VALUES ('PERMISO PERMANENCIA TERMPORAL','V') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_tipo_documento WHERE id_tipo_documento = 4) BEGIN INSERT INTO preciso_administracion_tipo_documento (nombre_tipo_documento,codigo_tipo_documento) VALUES ('NIP','0') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_tipo_documento WHERE id_tipo_documento = 5) BEGIN INSERT INTO preciso_administracion_tipo_documento (nombre_tipo_documento,codigo_tipo_documento) VALUES ('CEDULA DE CIUDADANIA','1') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_tipo_documento WHERE id_tipo_documento = 6) BEGIN INSERT INTO preciso_administracion_tipo_documento (nombre_tipo_documento,codigo_tipo_documento) VALUES ('CEDULA EXTRANJERIA','2') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_tipo_documento WHERE id_tipo_documento = 7) BEGIN INSERT INTO preciso_administracion_tipo_documento (nombre_tipo_documento,codigo_tipo_documento) VALUES ('NIT PERSONA JURIDICA(NACIONAL)','3') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_tipo_documento WHERE id_tipo_documento = 8) BEGIN INSERT INTO preciso_administracion_tipo_documento (nombre_tipo_documento,codigo_tipo_documento) VALUES ('TARJETA DE IDENTIDAD','4') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_tipo_documento WHERE id_tipo_documento = 9) BEGIN INSERT INTO preciso_administracion_tipo_documento (nombre_tipo_documento,codigo_tipo_documento) VALUES ('PASAPORTE','5') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_tipo_documento WHERE id_tipo_documento = 10) BEGIN INSERT INTO preciso_administracion_tipo_documento (nombre_tipo_documento,codigo_tipo_documento) VALUES ('NIT EXTRANJERIA','6') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_tipo_documento WHERE id_tipo_documento = 11) BEGIN INSERT INTO preciso_administracion_tipo_documento (nombre_tipo_documento,codigo_tipo_documento) VALUES ('SOC. EXTRANJERAS SIN NIT','7') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_tipo_documento WHERE id_tipo_documento = 12) BEGIN INSERT INTO preciso_administracion_tipo_documento (nombre_tipo_documento,codigo_tipo_documento) VALUES ('IDENT FIDEICOMISO','8') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_tipo_documento WHERE id_tipo_documento = 13) BEGIN INSERT INTO preciso_administracion_tipo_documento (nombre_tipo_documento,codigo_tipo_documento) VALUES ('NIT PARA PERSONAS NATURALES','9') END

-- IF NOT EXISTS(SELECT * FROM preciso_administracion_usuarios WHERE codigo_usuario='PRECISOUSER') BEGIN INSERT INTO preciso_administracion_usuarios(centro,contra,correo,creacion,empresa,estado,primer_nombre,password_token,codigo_usuario) VALUES ('0000','$2a$10$Nlz1ECsmJ4r/CBiMPmFYr.guiQ0wO2iR0f6tK94aSFg5HmWiC6nYa','admin@admin.com',GETDATE(),'0013',1,'Administrador Nexco',NULL,'PRECISOUSER') END
--IF NOT EXISTS(SELECT * FROM preciso_administracion_usuarios WHERE codigo_usuario='PRECISOUSER') BEGIN INSERT INTO preciso_administracion_usuarios(contra,correo,creacion,primer_nombre,segundo_nombre,primer_apellido,segundo_apellido,password_token,codigo_usuario,id_tipo_documento,numero_documento,id_cargo) VALUES('$2a$10$Nlz1ECsmJ4r/CBiMPmFYr.guiQ0wO2iR0f6tK94aSFg5HmWiC6nYa','admin@admin.com',GETDATE(),'Pilar','Maritza','Llantoy','Sanchez',NULL,'PRECISOUSER',1,78945612,1) END


IF NOT EXISTS(SELECT * FROM preciso_administracion_perfiles WHERE nombre_perfil = 'ADMIN') BEGIN INSERT INTO preciso_administracion_perfiles ( nombre_perfil) VALUES ('ADMIN')END
IF NOT EXISTS(SELECT * FROM preciso_administracion_perfiles WHERE nombre_perfil = 'USER') BEGIN INSERT INTO preciso_administracion_perfiles ( nombre_perfil) VALUES ('USER') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_perfiles WHERE nombre_perfil = 'RESPONSABLE') BEGIN INSERT INTO preciso_administracion_perfiles ( nombre_perfil) VALUES ('RESPONSABLE') END

-- Add views to the database
IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Usuarios') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Usuarios','/admin/users',1,'Administración','Usuarios','NA') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Roles') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Roles','/profile/roles',1,'Administración','Roles','NA') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Cargos') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Cargos','/admin/cargos',1,'Administración','Cargos','NA') END

IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Países') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Países','/parametry/countries',1,'Parametría','Parametros Generales','Países') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Tipos Eventos') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Tipos Eventos','/parametry/eventType',1,'Parametría','Parametros Generales','Tipos Eventos') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Divisas') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Divisas','/parametry/currency',1,'Parametría','Parametros Generales','Divisas') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Sistemas Fuentes') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Sistemas Fuentes','/parametry/sourceSystem',1,'Parametría','Parametros Generales','Sistemas Fuentes') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Parametro General') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Parametro General','/parametry/generalParam',1,'Parametría','Parametros Generales','Parametro General') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Rutas Contables') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Rutas Contables','/parametric/accountingRoutes',1,'Parametría','Procesos Contables','Rutas Contables') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Cargue Contable') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Cargue Contable','/parametric/accountingLoad',1,'Parametría','Procesos Contables','Cargue Contable') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Conciliaciones') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Conciliaciones','/parametry/conciliation',1,'Parametría','Parametros Funcionales','Conciliaciones') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Rutas Conciliaciones') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Rutas Conciliaciones','/parametry/conciliationRoutes',1,'Parametría','Parametros Funcionales','Rutas Conciliaciones') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Cargue Inventarios') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Cargue Inventarios','/parametric/inventoryLoad',1,'Parametría','Procesos Funcionales','Cargue Inventarios') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Matriz de Eventos') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Matriz de Eventos','/parametric/eventMatrix',1,'Parametría','Parametros Funcionales','Matriz de Eventos') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Plantilla de Notas') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Plantilla de Notas','/parametric/noteTemplates',1,'Parametría','Parametros Funcionales','Plantilla de Notas') END
IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Ajuste Conciliación') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Ajuste Conciliación','/process/conciliationAdj',1,'Procesos','Proceso Conciliaciones','Ajuste Conciliación') END

--IF NOT EXISTS(SELECT * FROM preciso_administracion_vistas WHERE nombre = 'Ver Tipos Eventos') BEGIN INSERT INTO preciso_administracion_vistas (nombre, ruta, unica,menu_principal,sub_menu_p1,sub_menu_p2) VALUES ('Ver Tipos Eventos','/parametry/typeEvent',1,'Parametría','Parametros Generales','Tipos Eventos') END

--IF NOT EXISTS(SELECT * FROM preciso_administracion_user_rol WHERE id_usuario = 1 AND id_perfil=1) BEGIN INSERT INTO preciso_administracion_user_rol (id_usuario,id_perfil) VALUES (1,1) END


-- IF NOT EXISTS(SELECT * FROM preciso_administracion_rol_vista AS nrv, preciso_administracion_vistas AS nv WHERE nrv.id_perfil = 1 AND nv.nombre = 'Ver Cuadro Mando Intergrupo' AND nv.id_vista = nrv.id_vista) BEGIN INSERT INTO preciso_administracion_rol_vista (id_perfil,id_vista) VALUES (1,4) END
--IF NOT EXISTS(SELECT * FROM preciso_administracion_rol_vista AS nrv, preciso_administracion_vistas AS nv WHERE nrv.id_perfil = 1 AND nv.nombre = 'Ver Roles' AND nv.id_vista = nrv.id_vista) BEGIN INSERT INTO preciso_administracion_rol_vista (id_perfil,id_vista) VALUES (1,4) END

IF NOT EXISTS(SELECT * FROM preciso_administracion_rol_vista AS nrv, preciso_administracion_vistas AS nv WHERE nrv.id_perfil = 1 AND nv.nombre = 'Ver Roles' AND nv.id_vista = nrv.id_vista) BEGIN INSERT INTO preciso_administracion_rol_vista (id_perfil,id_vista, p_modificar, p_visualizar) VALUES (1,(SELECT id_vista FROM preciso_administracion_vistas WHERE nombre = 'Ver Roles'),1,1) END


IF NOT EXISTS(SELECT * FROM preciso_parametros_generales WHERE id_parametro = 1) BEGIN INSERT INTO preciso_parametros_generales (id_parametro,unidad_principal,unidad_secundaria) VALUES (1,'Centro Contable','Centro Contable') END
IF NOT EXISTS(SELECT * FROM preciso_parametros_generales WHERE id_parametro = 2) BEGIN INSERT INTO preciso_parametros_generales (id_parametro,unidad_principal,unidad_secundaria) VALUES (2,'Centro Contable','Centro') END
IF NOT EXISTS(SELECT * FROM preciso_parametros_generales WHERE id_parametro = 3) BEGIN INSERT INTO preciso_parametros_generales (id_parametro,unidad_principal,unidad_secundaria) VALUES (3,'Cuenta Contable','Cuenta Contable') END
IF NOT EXISTS(SELECT * FROM preciso_parametros_generales WHERE id_parametro = 4) BEGIN INSERT INTO preciso_parametros_generales (id_parametro,unidad_principal,unidad_secundaria) VALUES (4,'Cuenta Contable','Cuenta') END
IF NOT EXISTS(SELECT * FROM preciso_parametros_generales WHERE id_parametro = 5) BEGIN INSERT INTO preciso_parametros_generales (id_parametro,unidad_principal,unidad_secundaria) VALUES (5,'Cuenta Contable','Conto') END
IF NOT EXISTS(SELECT * FROM preciso_parametros_generales WHERE id_parametro = 6) BEGIN INSERT INTO preciso_parametros_generales (id_parametro,unidad_principal,unidad_secundaria) VALUES (6,'Cuenta y Divisa','Cuenta y Divisa') END
IF NOT EXISTS(SELECT * FROM preciso_parametros_generales WHERE id_parametro = 7) BEGIN INSERT INTO preciso_parametros_generales (id_parametro,unidad_principal,unidad_secundaria) VALUES (7,'Cuenta y Divisa','Cuenta') END
IF NOT EXISTS(SELECT * FROM preciso_parametros_generales WHERE id_parametro = 8) BEGIN INSERT INTO preciso_parametros_generales (id_parametro,unidad_principal,unidad_secundaria) VALUES (8,'Cuenta y Divisa','Divisa') END