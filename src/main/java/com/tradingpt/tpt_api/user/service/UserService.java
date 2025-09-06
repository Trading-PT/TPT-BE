package com.tradingpt.tpt_api.user.service;

import com.tradingpt.tpt_api.user.entity.Customer;
import com.tradingpt.tpt_api.user.entity.Trainer;
import com.tradingpt.tpt_api.user.entity.User;


public interface UserService {
    void ensureUnique(String username, String email, String phone);
    boolean existsByUsername(String username);

    User saveUser(User user);
    Customer saveCustomer(Customer customer);
    Trainer saveTrainer(Trainer trainer);
}
