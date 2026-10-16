package com.skillSwap.Service;

import com.skillSwap.Entity.CreditTxType;
import com.skillSwap.Exception.UserNotFoundException;
import com.skillSwap.Repository.CreditTransactionRepository;
import com.skillSwap.Repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The ONLY place study rooms pay credits. It reuses your existing CreditWalletService (no second reward system)
 * and applies a per-user daily cap so nobody can farm credits.
 */
@Service
public class StudyRewardService {

	private final UserRepository userRepository;
	private final CreditTransactionRepository txRepository;
	private final CreditWalletService wallet;
	private final int dailyCap;

	public StudyRewardService(
		UserRepository userRepository,
		CreditTransactionRepository txRepository,
		CreditWalletService wallet,
		@Value("${skillswap.study.daily-credit-cap:5}") int dailyCap
	) {
		this.userRepository = userRepository;
		this.txRepository = txRepository;
		this.wallet = wallet;
		this.dailyCap = dailyCap;
	}

	/** Must run inside the caller's transaction, so the "claim" and the payout succeed or fail together. */
	@Transactional(propagation = Propagation.MANDATORY)
	public int award(Integer userId, int requested, CreditTxType type, String description) {
		if (requested <= 0 || dailyCap <= 0) return 0;
		// lock this user's row: two rewards for the same user can't both read the same "earned today" total
		userRepository.findByIdForUpdate(userId).orElseThrow(() -> new UserNotFoundException(userId));
		long earnedToday = txRepository.sumSince(
			userId,
			List.of(CreditTxType.STUDY_ATTENDANCE, CreditTxType.STUDY_CHALLENGE),
			LocalDate.now().atStartOfDay()
		);
		int pay = (int) Math.min(requested, Math.max(0, dailyCap - earnedToday));
		if (pay > 0) wallet.credit(userId, pay, type, null, description);
		return pay;
	}
}
