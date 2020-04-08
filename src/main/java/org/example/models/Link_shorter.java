package org.example.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Link_shorter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String link, short_link;

    public Link_shorter(String link, String short_link) {
        this.link = link;
        this.short_link = short_link;
    }

    public Link_shorter() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getShort_link() {
        return short_link;
    }

    public void setShort_link(String short_link) {
        this.short_link = short_link;
    }
}
