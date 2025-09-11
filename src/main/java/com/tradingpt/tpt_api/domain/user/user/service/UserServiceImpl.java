package com.tradingpt.tpt_api.domain.user.user.service;

import com.tradingpt.tpt_api.domain.user.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.user.entity.Trainer;
import com.tradingpt.tpt_api.domain.user.user.entity.User;
import com.tradingpt.tpt_api.domain.user.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.user.repository.TrainerRepository;
import com.tradingpt.tpt_api.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final TrainerRepository trainerRepository;

    @Override
    public void ensureUnique(String u, String e, String p) { /* 중복 검사 */ }

    @Override
    public boolean existsByUsername(String username){
        return userRepository.existsByUsername(username);
    }

    @Override @Transactional
    public User saveUser(User user){
        return userRepository.save(user);
    }

    @Override @Transactional
    public Customer saveCustomer(Customer customer){
        return customerRepository.save(customer);
    }

    @Override @Transactional
    public Trainer saveTrainer(Trainer trainer){
        return trainerRepository.save(trainer);
    }
}
