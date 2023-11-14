package tacos;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name="Taco_Order")
public class TacoOrder implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Long id;

  private Date placedAt;

  @ManyToOne
  private User user;

  private String deliveryName;

  private String deliveryStreet;

  private String deliveryCity;

  private String deliveryState;

  private String deliveryZip;

  private String ccNumber;

  private String ccExpiration;

  private String ccCVV;

  @ManyToMany(targetEntity=Taco.class,cascade = CascadeType.PERSIST)
  private List<Taco> tacos = new ArrayList<>();

  public void addTaco(Taco design) {
    this.tacos.add(design);
  }

  @PrePersist
  void placedAt() {
    this.placedAt = new Date();
  }

}
