import { useState, useEffect, useRef } from 'react'

const API = 'http://localhost:8080'

export default function App() {
  const [status, setStatus] = useState(null)
  const [diretorio, setDiretorio] = useState('')
  const [indexando, setIndexando] = useState(false)
  const [msgIndex, setMsgIndex] = useState('')
  const [consulta, setConsulta] = useState('')
  const [resultado, setResultado] = useState(null)
  const [buscando, setBuscando] = useState(false)
  const debounceRef = useRef(null)

  async function carregarStatus() {
    try {
      const r = await fetch(`${API}/api/status`)
      setStatus(await r.json())
    } catch {
      setStatus({ erro: 'Backend offline (inicie o servidor Java na porta 8080)' })
    }
  }

  useEffect(() => { carregarStatus() }, [])

  async function indexar() {
    if (!diretorio.trim()) return
    setIndexando(true)
    setMsgIndex('')
    try {
      const r = await fetch(`${API}/api/indexar?dir=${encodeURIComponent(diretorio)}`, { method: 'POST' })
      const j = await r.json()
      if (j.ok) {
        setMsgIndex(`✓ ${j.arquivos} arquivos · ${j.documentos} documentos · ${j.palavras} palavras · ${j.tempoMs} ms`)
        carregarStatus()
      } else {
        setMsgIndex(`✗ ${j.erro}`)
      }
    } catch {
      setMsgIndex('✗ Falha ao contatar o backend')
    } finally {
      setIndexando(false)
    }
  }

  // Busca instantânea com debounce (~250 ms) — o backend responde em O(1).
  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current)
    if (!consulta.trim()) {
      setResultado(null)
      return
    }
    debounceRef.current = setTimeout(async () => {
      setBuscando(true)
      try {
        const r = await fetch(`${API}/api/buscar?q=${encodeURIComponent(consulta)}`)
        setResultado(await r.json())
      } catch {
        setResultado({ erro: 'Falha na busca' })
      } finally {
        setBuscando(false)
      }
    }, 250)
    return () => clearTimeout(debounceRef.current)
  }, [consulta])

  return (
    <div className="container">
      <header>
        <h1>🔎 Buscador de Arquivos</h1>
        <p className="sub">Índice invertido com mapa de dispersão · busca instantânea</p>
      </header>

      <section className="card">
        <h2>1. Indexar</h2>
        <div className="row">
          <input
            type="text"
            placeholder="Caminho do diretório (ex: C:\\meus-textos)"
            value={diretorio}
            onChange={(e) => setDiretorio(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && indexar()}
          />
          <button onClick={indexar} disabled={indexando}>
            {indexando ? 'Indexando…' : 'Indexar'}
          </button>
        </div>
        {msgIndex && <p className="msg">{msgIndex}</p>}
      </section>

      <section className="card">
        <h2>2. Buscar</h2>
        <input
          type="text"
          className="busca"
          placeholder="Digite uma ou mais palavras (todas precisam aparecer)"
          value={consulta}
          onChange={(e) => setConsulta(e.target.value)}
          autoFocus
        />

        {buscando && <p className="msg">buscando…</p>}

        {resultado && resultado.erro && <p className="msg erro">{resultado.erro}</p>}

        {resultado && !resultado.erro && (
          <div className="resultados">
            <p className="contagem">
              {resultado.totalDocumentos} documento(s)
              {resultado.consulta?.length > 0 && (
                <> para <strong>{resultado.consulta.join(' + ')}</strong></>
              )}
            </p>
            <ul>
              {resultado.documentos.map((d) => (
                <li key={d.id}>
                  <span className="id">#{d.id}</span> {d.caminho}
                </li>
              ))}
            </ul>
            {resultado.totalDocumentos === 0 && (
              <p className="vazio">Nenhum documento contém todas as palavras.</p>
            )}
          </div>
        )}
      </section>

      <footer>
        {status && status.erro && <span className="erro">{status.erro}</span>}
        {status && !status.erro && (
          <span>
            Índice: {status.indiceCarregado ? 'carregado' : 'vazio'} ·{' '}
            {status.documentos} documentos · {status.palavras} palavras ·{' '}
            fator de carga {Number(status.fatorCarga).toFixed(3)}
          </span>
        )}
      </footer>
    </div>
  )
}
