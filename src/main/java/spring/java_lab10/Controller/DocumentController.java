package spring.java_lab10.Controller;

import org.apache.log4j.Logger;
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
import spring.java_lab10.Model.User;
import spring.java_lab10.Repository.DocumentRepository;
import spring.java_lab10.Repository.UserRepository;
import spring.java_lab10.Service.DigitalSignatureService;
import spring.java_lab10.Service.EncryptionService;

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

    @Autowired
    private DigitalSignatureService digitalSignatureService;

    private final Logger LOGGER = Logger.getLogger(DocumentController.class);

    @GetMapping("/")
    public String index(@RequestParam(value = "", required = false) String search, Model model) {
        List<Document> documents;
        if (search != null && !search.isEmpty()) {
            documents = documentRepository.findByNameContainingIgnoreCase(search);
        } else {
            documents = documentRepository.findAll();
        }
        model.addAttribute("documents", documents);
        model.addAttribute("search", search);
        return "index";
    }

    @GetMapping("/verify/{id}")
    public String verifySignature(@PathVariable Long id, Model model) {
        Optional<Document> documentOptional = documentRepository.findById(id);
        String mess;

        if (documentOptional.isPresent()) {
            Document document = documentOptional.get();
            boolean isSignatureValid;
            try {
                isSignatureValid = digitalSignatureService.verify(encryptionService.decryptDocument(document.getContent()), document.getSignature(), document.getAuthor());
            } catch (Exception e) {
                mess = "Signature verification failed: " + e.getMessage();
                model.addAttribute("mess", mess);
                LOGGER.debug(mess);
                return index("", model);
            }

            if (isSignatureValid) {
                mess = "Signature is valid for " + documentOptional.get().getName();
                LOGGER.debug(mess);
            } else {
                mess = "Signature is not valid for " + documentOptional.get().getName();
                LOGGER.debug(mess);
            }
        } else {
            mess = "Document not found";
            LOGGER.debug(mess);
        }

        model.addAttribute("mess", mess);
        return index("", model);
    }

    @PostMapping("/upload")
    public String uploadDocument(@RequestParam("file") MultipartFile file, Model model) throws Exception {
        if (file.isEmpty()) {
            LOGGER.debug("File with name " + file.getOriginalFilename() + " is empty");
            model.addAttribute("mess", "File with name " + file.getOriginalFilename() + " is empty");
            return index("", model);
        }

        Document document = new Document();
        String originalFilename = file.getOriginalFilename();
        document.setName(originalFilename);
        String contentType = file.getContentType();
        document.setType(contentType);
        byte[] fileBytes = file.getBytes();
        byte[] encFileBytes = encryptionService.encryptDocument(fileBytes);
        document.setContent(encFileBytes);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authorName = authentication.getName();
        User author = userRepository.findByUsername(authorName);
        document.setAuthor(author);

        String signature = digitalSignatureService.sign(fileBytes);
        document.setSignature(signature);

        LOGGER.debug("File with name " + originalFilename + " has been uploaded by " + authorName);

        documentRepository.save(document);
        return "redirect:/";
    }

    @GetMapping("/view/{id}")
    public String viewContent(@PathVariable Long id, Model model) throws Exception {
        Optional<Document> result = documentRepository.findById(id);

        if (result.isPresent()) {
            Document document = result.get();
            User authenticatedUser = getAuthenticatedUser();
            if (!document.getAuthor().getDepartment().getId().equals(authenticatedUser.getDepartment().getId())) {
                return "redirect:/403";
            }

            byte[] decFileBytes = encryptionService.decryptDocument(document.getContent());
            String base64EncodedDocument = Base64.getEncoder().encodeToString(decFileBytes);
            model.addAttribute("documentName", document.getName());
            model.addAttribute("documentContent", base64EncodedDocument);
            model.addAttribute("documentType", document.getType());

            LOGGER.debug("File with name " + document.getName() + " has been viewed by " + authenticatedUser.getUsername());

            return "viewContent";

        } else {
            String mess = "Document not found";
            LOGGER.debug(mess);
            model.addAttribute("mess", mess);
            return index("", model);
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        Optional<Document> documentOptional = documentRepository.findById(id);

        if (documentOptional.isPresent()) {
            Document document = documentOptional.get();
            User authenticatedUser = getAuthenticatedUser();

            if (!document.getAuthor().getDepartment().getId().equals(authenticatedUser.getDepartment().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            byte[] decryptedContent;
            try {
                decryptedContent = encryptionService.decryptDocument(document.getContent());
            } catch (Exception e) {
                LOGGER.error("Error decrypting document with id " + id, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", document.getName());
            headers.setContentLength(decryptedContent.length);

            LOGGER.debug("File with name " + document.getName() + " has been downloaded by " + authenticatedUser.getUsername());

            return new ResponseEntity<>(decryptedContent, headers, HttpStatus.OK);
        } else {
            LOGGER.error("Document not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteDocument(@PathVariable Long id, Model model) {
        Optional<Document> documentOptional = documentRepository.findById(id);

        if (documentOptional.isPresent()) {
            Document document = documentOptional.get();
            User authenticatedUser = getAuthenticatedUser();
            if (!document.getAuthor().getDepartment().getId().equals(authenticatedUser.getDepartment().getId())) {
                return "redirect:/403";
            }

            documentRepository.deleteById(id);
            LOGGER.debug("Document with id " + id + " has been deleted by " + authenticatedUser.getUsername());
        } else {
            String mess = "Document not found";
            LOGGER.debug(mess);
            model.addAttribute("mess", mess);
            return index("", model);
        }
        return "redirect:/";
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

}
