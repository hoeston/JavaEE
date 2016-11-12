package Myannotation;

@Component(id = "user")
public class User {

	private Model1 user1Dao;

	@Autowired
	public Model2 user2Dao;


	@Autowired
	public void setUser1Dao(Model1 user1Dao) {
		this.user1Dao = user1Dao;
	}

	public void show() {
		user1Dao.show1();
		user2Dao.show2();
		System.out.println("Œ“ «User.");
	}
}
