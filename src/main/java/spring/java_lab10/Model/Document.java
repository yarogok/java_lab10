package spring.java_lab10.Model;

import javax.persistence.*;

@Entity
@Table(name = "Documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Document_ID")
    private Long id;

    @Column(name = "Document_Name", nullable = false)
    private String name;

    @Column(name = "Content", nullable = false, columnDefinition = "bytea")
    private byte[] content;

    @Column(name = "Document_Type", nullable = false)
    private String type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Author_ID", nullable = false)
    private User author;

    @Column(name = "Signature", nullable = false, columnDefinition = "bytea")
    private byte[] signature;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }
}

