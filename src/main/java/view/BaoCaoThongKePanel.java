package com.qlts.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.qlts.bus.ThongKeBUS;
import com.qlts.common.Theme;
import com.qlts.component.Dialogs;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;

public class BaoCaoThongKePanel extends JPanel {

    private final ThongKeBUS bus = new ThongKeBUS();
    private final DecimalFormat MONEY_FMT = new DecimalFormat("#,##0");

    private ChartPanel chartTrangThaiPanel;
    private ChartPanel chartLoaiPanel;
    private ChartPanel chartThietBiPanel;
    private ChartPanel chartYeuCauPanel;

    private JLabel lblTonKho, lblTongTaiSan, lblTongThietBi, lblYeuCauCho;

    public BaoCaoThongKePanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tabs.addTab("🏠 Tổng quan",  buildTabTongQuan());
        tabs.addTab("🏷 Tài sản",     buildTabTaiSan());
        tabs.addTab("💻 Thiết bị",    buildTabThietBi());
        tabs.addTab("📨 Yêu cầu",     buildTabYeuCau());

        add(tabs, BorderLayout.CENTER);
        refreshAll();
    }

    private JPanel buildTabTongQuan() {
        JPanel wrap = new JPanel(new BorderLayout(0, 12));
        wrap.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        lblTonKho      = cardValue("0 VNĐ");
        lblTongTaiSan  = cardValue("0");
        lblTongThietBi = cardValue("0");
        lblYeuCauCho   = cardValue("0");

        cards.add(card("📦 Giá trị tồn kho", lblTonKho,       new Color(0x1565C0)));
        cards.add(card("🏷 Tổng tài sản",    lblTongTaiSan,   new Color(0x6A1B9A)));
        cards.add(card("💻 Tổng thiết bị",    lblTongThietBi,  new Color(0xEF6C00)));
        cards.add(card("📨 Yêu cầu chờ",     lblYeuCauCho,    new Color(0xC62828)));

        chartTrangThaiPanel = emptyChartPanel();
        chartTrangThaiPanel.setPreferredSize(new Dimension(500, 320));

        JPanel chartWrap = new JPanel(new BorderLayout());
        chartWrap.setBorder(BorderFactory.createTitledBorder("Phân bố tài sản theo trạng thái"));
        chartWrap.add(chartTrangThaiPanel, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("↻ Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBackground(Theme.primary());
        btnRefresh.setForeground(Theme.textOn());
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc:8; borderWidth:0; focusWidth:0;");
        btnRefresh.addActionListener(e -> refreshAll());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actions.add(btnRefresh);

        wrap.add(cards, BorderLayout.NORTH);
        wrap.add(chartWrap, BorderLayout.CENTER);
        wrap.add(actions, BorderLayout.SOUTH);
        return wrap;
    }

    private JPanel buildTabTaiSan() {
        JPanel wrap = new JPanel(new GridLayout(1, 2, 12, 0));
        wrap.setBorder(new EmptyBorder(12, 12, 12, 12));

        chartLoaiPanel = emptyChartPanel();
        chartThietBiPanel = emptyChartPanel();

        JPanel p1 = new JPanel(new BorderLayout());
        p1.setBorder(BorderFactory.createTitledBorder("Tài sản theo loại"));
        p1.add(chartLoaiPanel, BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout());
        p2.setBorder(BorderFactory.createTitledBorder("Trạng thái tài sản"));
        p2.add(chartThietBiPanel, BorderLayout.CENTER);

        wrap.add(p1);
        wrap.add(p2);
        return wrap;
    }

    private JPanel buildTabThietBi() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(new EmptyBorder(12, 12, 12, 12));

        chartYeuCauPanel = emptyChartPanel();
        chartYeuCauPanel.setPreferredSize(new Dimension(800, 400));

        JPanel chartWrap = new JPanel(new BorderLayout());
        chartWrap.setBorder(BorderFactory.createTitledBorder("Yêu cầu theo trạng thái"));
        chartWrap.add(chartYeuCauPanel, BorderLayout.CENTER);

        wrap.add(chartWrap, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildTabYeuCau() {
        // Placeholder — sau Đợt 4 có thể thêm chart yêu cầu/bảo trì/thanh lý
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lbl = new JLabel(
                "<html><div style='text-align:center;color:#757575;font-size:14px'>"
                + "📊 Báo cáo mở rộng (Khấu hao / Bảo trì / Thanh lý)<br><br>"
                + "Sẽ hiển thị sau khi hoàn thành Đợt 4 — Thống kê</div></html>",
                SwingConstants.CENTER);
        wrap.add(lbl, BorderLayout.CENTER);
        return wrap;
    }

    private JLabel cardValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(Color.WHITE);
        return l;
    }

    private JPanel card(String title, JLabel valueLabel, Color bg) {
        JPanel p = new JPanel(new BorderLayout(4, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(new Color(0xE0E0E0));

        p.add(lblTitle, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    private ChartPanel emptyChartPanel() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createBarChart("", "", "", ds);
        chart.setBackgroundPaint(Color.WHITE);
        ChartPanel cp = new ChartPanel(chart);
        cp.setMouseWheelEnabled(true);
        cp.setBackground(Color.WHITE);
        cp.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));
        return cp;
    }

    public void refreshAll() {
        try {
            // Cards
            BigDecimal ton = bus.tongGiaTriTonKho();
            lblTonKho.setText(MONEY_FMT.format(ton) + " VNĐ");

            int slTs = bus.taiSanTheoTrangThai().values().stream().mapToInt(Integer::intValue).sum();
            int slTb = bus.thietBiTheoTrangThai().values().stream().mapToInt(Integer::intValue).sum();
            lblTongTaiSan.setText(String.valueOf(slTs));
            lblTongThietBi.setText(String.valueOf(slTb));

            Map<String, Integer> yc = bus.yeuCauTheoTrangThai();
            int choDuyet = yc.getOrDefault("Chờ duyệt", 0);
            lblYeuCauCho.setText(String.valueOf(choDuyet));

            // Charts
            chartTrangThaiPanel.setChart(createPieChart("Trạng thái tài sản", bus.taiSanTheoTrangThai()));
            chartLoaiPanel.setChart(createBarChart("Số lượng theo loại tài sản", "Loại", "Số lượng", bus.taiSanTheoLoai()));
            chartThietBiPanel.setChart(createPieChart("Trạng thái tài sản", bus.taiSanTheoTrangThai()));
            chartYeuCauPanel.setChart(createBarChart("Yêu cầu theo trạng thái", "Trạng thái", "Số lượng", yc));

        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    // ===== CHART HELPERS =====
    private JFreeChart createBarChart(String title, String catAxis, String valAxis,
                                       Map<String, Integer> data) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            ds.addValue(e.getValue(), "Số lượng", e.getKey());
        }
        JFreeChart chart = ChartFactory.createBarChart(
                title, catAxis, valAxis, ds,
                PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 15));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(0xE0E0E0));
        plot.setOutlineVisible(false);

        BarRenderer r = (BarRenderer) plot.getRenderer();
        r.setSeriesPaint(0, new Color(0x1565C0));
        r.setBarPainter(new StandardBarPainter());
        r.setShadowVisible(false);
        r.setMaximumBarWidth(0.08);
        return chart;
    }

    private JFreeChart createPieChart(String title, Map<String, Integer> data) {
        DefaultPieDataset<String> ds = new DefaultPieDataset<>();
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            ds.setValue(e.getKey(), e.getValue());
        }
        JFreeChart chart = ChartFactory.createPieChart(title, ds, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 15));

        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));

        Color[] colors = {
                new Color(0x2E7D32), new Color(0x1565C0), new Color(0xEF6C00),
                new Color(0xC62828), new Color(0x6A1B9A), new Color(0x00838F),
                new Color(0x558B2F), new Color(0xAD1457)
        };
        int i = 0;
        for (Object key : data.keySet()) {
            plot.setSectionPaint((Comparable<?>) key, colors[i % colors.length]);
            i++;
        }
        return chart;
    }
}