# Transferência de arquivos via SFTP com Java

Biblioteca utilizada [jsch](http://www.jcraft.com/jsch/)

Exemplo de utilização:

```java
SFTP sftp = new SFTP("usuario", "senha", "192.168.10.2", 22);
sftp.transferirDiretorio("/home/usuario/diretorioOrigem", "/tmp/diretorioDestino");
```


obs.: Essa classe criada remove o diretório destino caso ele já exista antes de realizar a cópia.
