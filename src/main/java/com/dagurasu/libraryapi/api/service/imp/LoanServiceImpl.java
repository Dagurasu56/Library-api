package com.dagurasu.libraryapi.api.service.imp;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dagurasu.libraryapi.api.dto.LoanFilterDTO;
import com.dagurasu.libraryapi.api.model.entity.Book;
import com.dagurasu.libraryapi.api.model.entity.Loan;
import com.dagurasu.libraryapi.api.model.repository.LoanRepository;
import com.dagurasu.libraryapi.api.service.LoanService;
import com.dagurasu.libraryapi.exception.BusinessException;

@Service
public class LoanServiceImpl implements LoanService {

	private LoanRepository repository;

	public LoanServiceImpl(LoanRepository repository) {
		this.repository = repository;
		
	}
	
	@Override
	public Loan save(Loan loan) {
		if(repository.existsByBookAndNotReturned(loan.getBook())) {
			throw new BusinessException("Book already loaned");
		}
		return repository.save(loan);
	}

	@Override
	public Optional<Loan> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public Loan update(Loan loan) {
		return repository.save(loan);
	}

	@Override
	public Page<Loan> find(LoanFilterDTO filterDTO, Pageable pageable) {
		return repository.findByBookIsbnOrCustomer(filterDTO.getIsbn(), filterDTO.getCustomer(), pageable);
	}

	@Override
	public Page<Loan> getLoansByBook(Book book, Pageable pageable) {
		return repository.findByBook(book, pageable);
	}

}
