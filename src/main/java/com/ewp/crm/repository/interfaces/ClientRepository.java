package com.ewp.crm.repository.interfaces;

import com.ewp.crm.models.Client;
import com.ewp.crm.models.SocialProfile;
import com.ewp.crm.models.Status;
import com.ewp.crm.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientRepository extends CommonGenericRepository<Client>, ClientRepositoryCustom {

	Client getClientBySocialProfiles(List<SocialProfile> list);

	List<Client> getClientsByOwnerUser(User ownerUser);

	List<Client> getAllByStatus(Status status);

	Client getClientBySkype(String skypeLogin);

	Client getClientByEmail(String Email);

	Client getClientByPhoneNumber(String phoneNumber);

	List<Client> getById(List<Long> ids);

	Page<Client> findAll(Pageable pageable);

	Page<Client> getAllByOwnerUser(Pageable pageable, User clientOwner);
}
