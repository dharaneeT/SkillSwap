package com.skillSwap.Service;

import com.skillSwap.Dto.credit.CreditTransactionDTO;
import com.skillSwap.Entity.CreditTransaction;
import com.skillSwap.Entity.CreditTxType;
import com.skillSwap.Exception.CreditException;
import com.skillSwap.Exception.UserNotFoundException;
import com.skillSwap.Repository.CreditTransactionRepository;
import com.skillSwap.Repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditWalletService {

	private final UserRepository userRepository;
	private final CreditTransactionRepository txRepository;

	public CreditWalletService(UserRepository userRepository, CreditTransactionRepository txRepository) {
		this.userRepository = userRepository;
		this.txRepository = txRepository;
	}

	@Transactional
	public CreditTransaction credit(
		Integer userId,
		int amount,
		CreditTxType type,
		Integer sessionId,
		String description
	) {
		requirePositive(amount);
		return apply(userId, amount, type, sessionId, description);
	}

	@Transactional
	public CreditTransaction debit(
		Integer userId,
		int amount,
		CreditTxType type,
		Integer sessionId,
		String description
	) {
		requirePositive(amount);
		return apply(userId, -amount, type, sessionId, description);
	}

	@Transactional(readOnly = true)
	public int getBalance(Integer userId) {
		Integer c = userRepository.findCredits(userId);
		return c == null ? 0 : c;
	}

	@Transactional(readOnly = true)
	public List<CreditTransactionDTO> history(Integer userId) {
		return txRepository
			.findByUser_IdOrderByCreatedAtDescIdDesc(userId)
			.stream()
			.map(t ->
				new CreditTransactionDTO(
					t.getId(),
					t.getType(),
					t.getAmount(),
					t.getBalanceAfter(),
					t.getSessionId(),
					t.getDescription(),
					t.getCreatedAt()
				)
			)
			.toList();
	}

	private CreditTransaction apply(
		Integer userId,
		int delta,
		CreditTxType type,
		Integer sessionId,
		String description
	) {
		int updated = userRepository.applyCreditDelta(userId, delta);
		if (updated == 0) {
			if (!userRepository.existsById(userId)) throw new UserNotFoundException(userId);
			throw new CreditException("Insufficient credits (need " + (-delta) + ", have " + getBalance(userId) + ")");
		}
		CreditTransaction t = new CreditTransaction();
		t.setUser(userRepository.getReferenceById(userId));
		t.setType(type);
		t.setAmount(delta);
		t.setBalanceAfter(userRepository.findCredits(userId));
		t.setSessionId(sessionId);
		t.setDescription(description);
		return txRepository.save(t);
	}

	private void requirePositive(int amount) {
		if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than zero");
	}
}
