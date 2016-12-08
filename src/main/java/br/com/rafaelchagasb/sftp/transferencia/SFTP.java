package br.com.rafaelchagasb.sftp.transferencia;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTP {
	
	private String usuario;
	
	private String senha;
	
	private String host;
	
	private int porta;
	
	public SFTP(String usuario, String senha, String host, int porta) {
		this.usuario = usuario;
		this.senha = senha;
		this.host = host;
		this.porta = porta;
	}
	
	public void transferirDiretorio(String diretorioOrigem, String diretorioDestino){
		
		try {
			
			ChannelSftp c = getChannel();
		
			removerDiretorioRemoto(c, diretorioDestino);
			
	        criarPastaRemota(c, diretorioDestino);
				
	       	transferir(c, diretorioOrigem, diretorioDestino);
	       	
	       	fecharConexao(c);
	        
        
		} catch (JSchException e) {
		
			e.printStackTrace();
			
			new RuntimeException("Não foi possível copiar os arquivos da pasta: " + diretorioOrigem + " para a pasta " + diretorioDestino, e);
		
		} catch (SftpException e) {
		
			e.printStackTrace();
			
			new RuntimeException("Não foi possível copiar os arquivos da pasta: " + diretorioOrigem + " para a pasta " + diretorioDestino, e);
		
		}
        
	}
	
	private void removerDiretorioRemoto(ChannelSftp c, String diretorioDestino) throws SftpException {
	
		if(existeDiretorioRemoto(c, diretorioDestino)){
        	
        	removerDiretorio(c, diretorioDestino);
		
        }
		
	}

	private void criarPastaRemota(ChannelSftp c, String diretorioDestino) throws SftpException {
		
		c.mkdir(diretorioDestino);
		
	}

	private void fecharConexao(ChannelSftp c) throws JSchException {
		
		c.getSession().disconnect();
		
		c.disconnect();

	}

	private ChannelSftp getChannel() throws JSchException{
		
		JSch jsch = new JSch();
		Properties config = new Properties();
		config.put("cipher.s2c", "aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc,aes192-ctr,aes192-cbc,aes256-ctr,aes256-cbc");
		config.put("cipher.c2s", "aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc,aes192-ctr,aes192-cbc,aes256-ctr,aes256-cbc");
		config.put("kex", "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256");
		
		Session jschSession;
	
		jschSession = jsch.getSession(usuario, host, porta);

		jschSession.setConfig("StrictHostKeyChecking", "no");
		jschSession.setPassword(senha);
		jschSession.setConfig(config);
		jschSession.connect();
		
		Channel channel =  jschSession.openChannel("sftp");
         
		channel.connect();
         
        return (ChannelSftp) channel;
	}
	
	private void transferir(ChannelSftp c, String diretorioOrigem, String caminhoDestino) throws SftpException{
		
		File diretorio = new File(diretorioOrigem);
		
		for(File arquivoOrigem : Arrays.asList(diretorio.listFiles())){
       		
       		if(isArquivo(arquivoOrigem)){
       			
       			c.put(arquivoOrigem.getAbsolutePath(), caminhoDestino);
       			
       		} else{
       			
       			String nomePastaDestino = caminhoDestino + File.separatorChar + arquivoOrigem.getName();
       			
       			criarPastaRemota(c, nomePastaDestino);
       			
       			transferir(c, arquivoOrigem.getAbsolutePath(), nomePastaDestino);
       			
       		}
       	}
		
	}
	
	private void removerDiretorio(ChannelSftp sftp, String caminhoRemoto) throws SftpException {
		
		if (isDiretorio(sftp, caminhoRemoto)){
			
			sftp.cd(caminhoRemoto);
			
			Vector <LsEntry > entradas = sftp.ls(".");
			
			for (LsEntry entrada: entradas) {
				
				if(!entrada.getFilename().equals(".") && !entrada.getFilename().equals("..")){
					
					removerDiretorio(sftp, entrada.getFilename());
					
				}
	        }

			sftp.cd("..");
	        
			sftp.rmdir(caminhoRemoto);
		
		} else{
			
			sftp.rm(caminhoRemoto);
			
		}
	}

	private boolean isDiretorio(ChannelSftp sftp, String entry) throws SftpException {
	    
		return sftp.stat(entry).isDir();
	
	}
	
	private boolean existeDiretorioRemoto(ChannelSftp c, String caminhoRemoto){

        try {
            
        	c.stat(caminhoRemoto);
            
            return true;
        
        } catch (Exception e) {
        
        	return false;
        
        }
	}
	
	private boolean isArquivo(File arquivoOrigem) {
	
		return arquivoOrigem.isFile();
	}
	
}
