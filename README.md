# servidorChat
En esta rama implementamos que el servidor solo se encargue de ayudar a dos clientes a intercambiar ip's y puertos, para que estos hablen sin interactuar con el servidor. El servidor tambien aporta el servicio de mensajes pendientes, si un usuario no se puede alcanzar, el mensaje se le pasa al servidor y este lo envia cuando pueda al otro usuario.
La idea es que el servidor tenga la minima cantidad de tareas y overhead posible.
Toda comunicacion entre un usuario y el servidor esta autenticada y el trafico es encriptado con AES128.
