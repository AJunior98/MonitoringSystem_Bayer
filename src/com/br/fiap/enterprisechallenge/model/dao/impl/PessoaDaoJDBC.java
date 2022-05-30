package com.br.fiap.enterprisechallenge.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.br.fiap.enterprisechallenge.database.DB;
import com.br.fiap.enterprisechallenge.database.DbException;
import com.br.fiap.enterprisechallenge.model.dao.PessoaDao;
import com.br.fiap.enterprisechallenge.model.entites.Doenca;
import com.br.fiap.enterprisechallenge.model.entites.Pessoa;

public class PessoaDaoJDBC implements PessoaDao {

	private Connection conn;
	
	public PessoaDaoJDBC(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public void insert(Pessoa obj) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"INSERT INTO tb_pessoa " 
					+ "(Mes, Regiao, Genero, Idade, DoencaId) "
					+ "VALUES "
					+ "(?,?,?,?,?)", 
					Statement.RETURN_GENERATED_KEYS);
			
			st.setString(1, obj.getMes());
			st.setString(2, obj.getRegiao());
			st.setString(3, obj.getGenero());
			st.setInt(4, obj.getIdade());
			st.setInt(5, obj.getDoenca().getId());
			
			int rowsAffected = st.executeUpdate();
			
			if (rowsAffected > 0) {
				ResultSet rs = st.getGeneratedKeys();
				if(rs.next()) {
					int id = rs.getInt(1);
					obj.setId(id);
				}
				DB.closeResultSet(rs);
			}
			else {
				throw new DbException("Unexpected error! No rows affected.");
			}

		}
		catch (SQLException e) {
			throw new DbException(e.getMessage());
		} 
		finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void update(Pessoa obj) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(
					"UPDATE tb_pessoa " 
					+ "SET Mes = ?, Regiao = ?, Genero = ?, Idade = ?, DoencaId = ? "
					+ "WHERE Id = ?");
			
			st.setString(1, obj.getMes());
			st.setString(2, obj.getRegiao());
			st.setString(3, obj.getGenero());
			st.setInt(4, obj.getIdade());
			st.setInt(5, obj.getDoenca().getId());
			st.setInt(6, obj.getId());
			
			st.executeUpdate();
			
		}
		catch (SQLException e) {
			throw new DbException(e.getMessage());
		} 
		finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void deleteById(Integer id) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("DELETE FROM tb_pessoa WHERE Id = ?");
			
			st.setInt(1, id);
			
			st.executeUpdate();
		}
		catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public Pessoa findById(Integer id) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					"SELECT tb_pessoa.*, tb_doenca.Tipo as doencaName "
					+ "FROM tb_pessoa INNER JOIN tb_doenca "
					+ "ON tb_pessoa.DoencaId = tb_doenca.Id "
					+ "where tb_pessoa.Id = ?");
					
			st.setInt(1, id);
			rs = st.executeQuery();
			if (rs.next()) {
				Doenca dc = instantiateDoenca(rs);
				Pessoa obj = instantiatePessoa(rs, dc);
				return obj;
			}
			return null;
		}
		catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	private Pessoa instantiatePessoa(ResultSet rs, Doenca dc) throws SQLException {
		Pessoa obj = new Pessoa();
		obj.setId(rs.getInt("Id"));
		obj.setRegiao(rs.getString("Regiao"));
		obj.setMes(rs.getString("Mes"));
		obj.setGenero(rs.getString("Genero"));
		obj.setIdade(rs.getInt("Idade"));
		obj.setDoenca(dc);
		return obj;
	}

	private Doenca instantiateDoenca(ResultSet rs) throws SQLException {
		Doenca dc = new Doenca();
		dc.setId(rs.getInt("DoencaId"));
		dc.setNome(rs.getString("doencaName"));
		return dc;
	}

	@Override
	public List<Pessoa> findAll() {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					"SELECT tb_pessoa.*, tb_doenca.Tipo as doencaName "
					+ "FROM tb_pessoa INNER JOIN tb_doenca "
					+ "ON tb_pessoa.DoencaId = tb_doenca.Id "
					+ "ORDER BY Id");
					
			rs = st.executeQuery();
			
			List<Pessoa> list = new ArrayList<>();
			Map<Integer, Doenca> map = new HashMap<>();
			
			while (rs.next()) {
				
				Doenca dc = map.get(rs.getInt("DoencaId"));
				
				if (dc == null) {
					dc = instantiateDoenca(rs);
					map.put(rs.getInt("DoencaId"), dc);
				}
				
				Pessoa obj = instantiatePessoa(rs, dc);
				list.add(obj);
			}
			return list;
		}
		catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public List<Pessoa> findByDoenca(Doenca doenca) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					"SELECT tb_pessoa.*, tb_doenca.Tipo as doencaName "
					+ "FROM tb_pessoa INNER JOIN tb_doenca "
					+ "ON tb_pessoa.DoencaId = tb_doenca.Id WHERE DoencaId = ? "
					+ "ORDER BY Id");
					
			st.setInt(1, doenca.getId());
			rs = st.executeQuery();
			
			List<Pessoa> list = new ArrayList<>();
			Map<Integer, Doenca> map = new HashMap<>();
			
			while (rs.next()) {
				
				Doenca dc = map.get(rs.getInt("DoencaId"));
				
				if (dc == null) {
					dc = instantiateDoenca(rs);
					map.put(rs.getInt("DoencaId"), dc);
				}
				
				Pessoa obj = instantiatePessoa(rs, dc);
				list.add(obj);
			}
			return list;
		}
		catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

}
