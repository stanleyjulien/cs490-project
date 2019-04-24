package mum.pmp.mstore.adapters;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import mum.pmp.mstore.model.Person;
import mum.pmp.mstore.model.User;

public class UserAdapter implements UserDetails {
	private static final long serialVersionUID = -1360188483928178893L;
	private User user;
	private Person person;

	public UserAdapter(User user) {
		this.user = user;
	}

	public UserAdapter(User user, Person person) {
		this.user = user;
		this.person = person;
		System.out.println("user details:" + user);
		System.out.println("person details:" + person.getEmail() + ","  + person.getPassword());
		getAuthorities();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		System.out.println("Get authroities method.");
		Set<GrantedAuthority> authorities = new HashSet<>();
		user.getRoles().stream().forEach(authorities::add);
		user.getRoles().forEach(System.out::print);
		
		return authorities;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}

	public User getUser() {
		return user;
	}

	public Person getPerson() {
		return person;
	}
}