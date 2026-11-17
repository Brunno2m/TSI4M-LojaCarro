package br.org.edu.ifrn.LojaCarro.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

@Service
public class ImagemService {

    private static final long LIMITE_BYTES = 5L * 1024 * 1024;
    private static final long LIMITE_PIXELS = 25_000_000L;
    private final Path diretorio;

    public ImagemService(@Value("${app.imagens.diretorio:IMG}") String pasta) {
        this.diretorio = Path.of(pasta).toAbsolutePath().normalize();
    }

    public String salvar(MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A fotografia é obrigatória.");
        }
        if (foto.getSize() > LIMITE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "A fotografia deve ter até 5 MB.");
        }

        byte[] bytes;
        try {
            bytes = foto.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível ler a fotografia.", e);
        }
        if (bytes.length == 0 || bytes.length > LIMITE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tamanho de fotografia inválido.");
        }

        String extensao = validarImagem(bytes);
        String nome = UUID.randomUUID() + "." + extensao;
        Path destino = diretorio.resolve(nome);
        try {
            Files.createDirectories(diretorio);
            Files.write(destino, bytes, StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível armazenar a fotografia.", e);
        }
        // Caminho relativo armazenado no banco (independente da máquina).
        return "IMG/" + nome;
    }

    private String validarImagem(byte[] bytes) {
        try (ImageInputStream entrada = ImageIO.createImageInputStream(
                new ByteArrayInputStream(bytes))) {
            if (entrada == null) {
                throw imagemInvalida();
            }
            Iterator<ImageReader> leitores = ImageIO.getImageReaders(entrada);
            if (!leitores.hasNext()) {
                throw imagemInvalida();
            }
            ImageReader leitor = leitores.next();
            try {
                String formato = leitor.getFormatName().toLowerCase(Locale.ROOT);
                String extensao = switch (formato) {
                    case "jpeg", "jpg" -> "jpg";
                    case "png" -> "png";
                    case "webp" -> "webp";
                    default -> throw imagemInvalida();
                };
                leitor.setInput(entrada, true, true);
                int largura = leitor.getWidth(0);
                int altura = leitor.getHeight(0);
                if (largura <= 0 || altura <= 0
                        || (long) largura * altura > LIMITE_PIXELS) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Dimensões da fotografia inválidas ou excessivas.");
                }
                if (leitor.read(0) == null) {
                    throw imagemInvalida();
                }
                return extensao;
            } finally {
                leitor.dispose();
            }
        } catch (IOException | IllegalArgumentException e) {
            throw imagemInvalida();
        }
    }

    public Resource carregar(String caminho) {
        Path arquivo = resolver(caminho);
        if (!Files.isRegularFile(arquivo, LinkOption.NOFOLLOW_LINKS)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Fotografia não encontrada.");
        }
        return new FileSystemResource(arquivo);
    }

    public MediaType tipo(String caminho) {
        resolver(caminho);
        if (caminho.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (caminho.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }

    // Usado somente para limpar um novo upload quando o cadastro/edição falhar.
    public void apagarNovoArquivo(String caminho) {
        try {
            Files.deleteIfExists(resolver(caminho));
        } catch (IOException | ResponseStatusException e) {
            // Não substitui o erro original da operação.
            org.slf4j.LoggerFactory.getLogger(ImagemService.class)
                    .warn("Falha ao limpar fotografia não associada.");
        }
    }

    private Path resolver(String caminho) {
        if (caminho == null || !caminho.matches(
                "IMG/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|webp)")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Fotografia não encontrada.");
        }
        Path arquivo = diretorio.resolve(caminho.substring(4)).normalize();
        if (!arquivo.startsWith(diretorio)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Fotografia não encontrada.");
        }
        return arquivo;
    }

    private ResponseStatusException imagemInvalida() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Envie uma fotografia válida em JPG, JPEG, PNG ou WebP.");
    }
}