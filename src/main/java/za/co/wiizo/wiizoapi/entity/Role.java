package za.co.wiizo.wiizoapi.entity;

import javax.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;


        @Column(length = 60)
        private RoleName name;

        public RoleName getName() {
                return name;
        }

        public void setName(RoleName name) {
                this.name = name;
        }
}
