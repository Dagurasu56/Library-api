package com.dagurasu.libraryapi.model.repository;

import static com.dagurasu.libraryapi.model.repository.BookRepositoryTest.createNewBook;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dagurasu.libraryapi.api.model.entity.Book;
import com.dagurasu.libraryapi.api.model.entity.Loan;
import com.dagurasu.libraryapi.api.model.repository.LoanRepository;


@DataJpaTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class LoanRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private LoanRepository repository;

	@Test
	@DisplayName("Deve verificar se existe empréstimo não devolvido para o livro ")
	public void existsBookByAndNotReturnedTest() {
		
		Loan loan = createAndPersistLoan(LocalDate.now());
		Book book = loan.getBook();
		
		boolean exists = repository.existsByBookAndNotReturned(book);
		
		assertThat(exists).isTrue();
	}
	
	@Test
	@DisplayName("Deve buscar empréstimo pelo isbn do livro ou customer")
	public void findByBookIsbnOrCustomerTest() {
		
		Loan loan = createAndPersistLoan(LocalDate.now());
		
		Page<Loan> result = repository.findByBookIsbnOrCustomer("123", "Fulano", PageRequest.of(0, 10));

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent()).contains(loan);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getTotalElements()).isEqualTo(1);
		
	}
	
	@Test
	@DisplayName("Deve obter empréstimos cuja data empréstimo for menor ou igual a três dias atrás e não retornados.")
	public void findByLoansDateLessThanAndNotReturned() {
		Loan loan = createAndPersistLoan(LocalDate.now().minusDays(5));
		
		List<Loan> result = repository.findByLoansDateLessThanAndNotReturned(LocalDate.now().minusDays(4));
		
		assertThat(result).hasSize(1).contains(loan);
		
	}
	
	@Test
	@DisplayName("Deve retornar vazio quando não houver empréstimos atrasados.")
	public void notFindByLoansDateLessThanAndNotReturned() {
		createAndPersistLoan(LocalDate.now());
		
		List<Loan> result = repository.findByLoansDateLessThanAndNotReturned(LocalDate.now().minusDays(4));
		
		assertThat(result).isEmpty();
		
	}
	
	public Loan createAndPersistLoan(LocalDate loanDate) {
		Book book = createNewBook("123");
		entityManager.persist(book); 
		
		Loan loan = Loan.builder()
				.book(book)
				.customer("Fulano")
				.loanDate(loanDate)
				.build();
		entityManager.persist(loan);
		
		return loan;
	}

}
