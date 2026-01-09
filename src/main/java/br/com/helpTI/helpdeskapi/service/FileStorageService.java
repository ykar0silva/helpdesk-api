package br.com.helpTI.helpdeskapi.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.net.MalformedURLException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

@Service
public class FileStorageService {

    private final Path fileStorageLocation; // O caminho que definimos no properties

    // 1. Construtor: Pega o caminho do properties e o armazena
    public FileStorageService(@Value("${storage.location}") String storageLocation) {
        this.fileStorageLocation = Paths.get(storageLocation).toAbsolutePath().normalize();

        // 2. Tenta criar o diretório, se ele não existir
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads.", ex);
        }
    }

    // 3. Método principal: Salva o arquivo no disco
    public String storeFile(MultipartFile file) {
        // Pega o nome original (ex: "print_erro.png")
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // 4. Gera um nome de arquivo ÚNICO (para evitar sobreposição)
            // Ex: "print_erro.png" vira "a31f-b421-....-print_erro.png"
            String extension = StringUtils.getFilenameExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID().toString() + "." + extension;

            // 5. Define o caminho completo de destino
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);

            // 6. Copia o arquivo (stream) para o local de destino
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 7. Retorna o NOME ÚNICO que foi salvo
            return uniqueFileName;

        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível salvar o arquivo " + originalFileName, ex);
        }
    }


    public Resource loadFileAsResource(String fileName) {
        try {
            // Monta o caminho completo: uploads/nome-do-arquivo.png
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            
            // Transforma o caminho em um Recurso (Resource) do Spring
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Arquivo não encontrado: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Arquivo não encontrado " + fileName, ex);
        }
    }
}