package spring.java_lab10.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spring.java_lab10.Model.Document;
import spring.java_lab10.Model.Role;
import spring.java_lab10.Model.User;
import spring.java_lab10.Repository.DocumentRepository;
import spring.java_lab10.Repository.UserRepository;
import spring.java_lab10.Service.EncryptionService;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Controller
public class DocumentController {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EncryptionService encryptionService;

    @GetMapping("/")
    public String index(Model model) {
        List<Document> documents = documentRepository.findAll();
        model.addAttribute("documents", documents);
        return "index";
    }

    @PostMapping("/upload")
    public String uploadDocument(@RequestParam("file") MultipartFile file) throws Exception {
        Document document = new Document();
        String originalFilename = file.getOriginalFilename();
        document.setName(originalFilename);
        String contentType = file.getContentType();
        document.setType(contentType);
        byte[] fileBytes = file.getBytes();
        byte[] encFileBytes = encryptionService.encryptDocument(fileBytes);
        document.setContent(encFileBytes);

        /*Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authorName = authentication.getName();
        User author = userRepository.findByUsername(authorName);
        document.setAuthor(author);*/
        documentRepository.save(document);
        return "redirect:/";
    }

    @GetMapping("/view/{id}")
    public String viewContent(@PathVariable Long id, Model model) throws Exception {
        Optional<Document> result = documentRepository.findById(id);
        if (result.isPresent()) {
            Document document = result.get();
            byte[] decFileBytes = encryptionService.decryptDocument(document.getContent());
            String base64EncodedDocument = Base64.getEncoder().encodeToString(decFileBytes);
            model.addAttribute("documentName", document.getName());
            model.addAttribute("documentContent", base64EncodedDocument);
            model.addAttribute("documentType", document.getType()); // assuming you have a method to get document type
            return "viewContent";
        }
        return "redirect:/";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        Optional<Document> documentOptional = documentRepository.findById(id);

        if (documentOptional.isPresent()) {
            Document document = documentOptional.get();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", document.getName());
            headers.setContentLength(document.getContent().length);

            return new ResponseEntity<>(document.getContent(), headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteDocument(@PathVariable Long id) {
        documentRepository.deleteById(id);
        return "redirect:/";
    }
}
